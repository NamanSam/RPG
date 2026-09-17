param([string]$BaseUrl='http://127.0.0.1:8085')
$ErrorActionPreference='Stop'
function Assert($condition,$message) { if(-not $condition) { throw $message } }
$session=New-Object Microsoft.PowerShell.Commands.WebRequestSession
function Headers { $c=Invoke-RestMethod "$BaseUrl/api/auth/csrf" -WebSession $session; $h=@{}; $h[$c.headerName]=$c.token; return $h }
function Post($path,$body) { Invoke-RestMethod "$BaseUrl/api$path" -Method Post -ContentType 'application/json' -WebSession $session -Headers (Headers) -Body ($body|ConvertTo-Json) }
function Get($path) { Invoke-RestMethod "$BaseUrl/api$path" -WebSession $session }
function Locked($id,$answer) {
    $r=Invoke-WebRequest "$BaseUrl/api/quests/$id/submit" -Method Post -ContentType 'application/json' -WebSession $session -Headers (Headers) -Body (@{answer=$answer}|ConvertTo-Json) -SkipHttpErrorCheck
    Assert ($r.StatusCode -eq 403) "Locked quest submission accepted: $id"
    $r=Invoke-WebRequest "$BaseUrl/api/quests/$id" -WebSession $session -SkipHttpErrorCheck
    Assert ($r.StatusCode -eq 403) "Locked detail exposed: $id"
}
$quests=@(
    @{id='first-variable';answer='5';reward=50;world=1},
    @{id='choose-the-type';answer='boolean';reward=50;world=1},
    @{id='operator-training';answer='2';reward=75;world=1},
    @{id='the-gatekeeper';answer='coins >= 10';reward=75;world=2},
    @{id='the-looping-mill';answer='for (int i = 0; i < 3; i++)';reward=100;world=2},
    @{id='the-endless-well';answer='8';reward=125;world=2},
    @{id='the-lost-index';answer='9';reward=100;world=3},
    @{id='forest-traversal';answer='10';reward=125;world=3},
    @{id='hidden-maximum';answer='-3';reward=150;world=3},
    @{id='blueprint-maker';answer='Bridge bridge = new Bridge();';reward=125;world=4},
    @{id='the-hidden-vault';answer='private';reward=150;world=4},
    @{id='bloodline-of-classes';answer='extends';reward=175;world=4},
    @{id='many-forms';answer='Rope';reward=200;world=4},
    @{id='the-dynamic-scroll';answer='3';reward=150;world=5},
    @{id='the-key-keeper';answer='7';reward=175;world=5},
    @{id='hall-of-uniques';answer='3';reward=200;world=5}
)
$email="java-smoke-$([Guid]::NewGuid().ToString('N'))@codequest.test"
$password=[Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(16))
$new=Post '/auth/register' @{email=$email;password=$password;displayName='Java Campaign Explorer'}
Assert ($new.totalXp -eq 0) 'Starting XP is not zero.'
$total=0
for($i=0;$i -lt $quests.Count;$i++) {
    $q=$quests[$i]
    if($i -lt 15) { $map=Get '/campaigns/dsa/map'; Assert (-not $map.unlocked) 'DSA unlocked early.' }
    if($i -lt 15) { $locked=$quests[$i+1]; Locked $locked.id $locked.answer }
    $bad=Post "/quests/$($q.id)/submit" @{answer='wrong'}
    Assert ($bad.awardedXp -eq 0 -and $bad.progress.totalXp -eq $total) 'Incorrect answer changed XP.'
    $total+=$q.reward
    $isFinal=$i -in @(8,12,15)
    if($isFinal) {
        $h=Headers; $h.Cookie=$session.Cookies.GetCookieHeader([Uri]$BaseUrl);$id=$q.id;$answer=$q.answer
        $results=1..2|ForEach-Object -Parallel { Invoke-RestMethod "$using:BaseUrl/api/quests/$using:id/submit" -Method Post -ContentType 'application/json' -Headers $using:h -Body (@{answer=$using:answer}|ConvertTo-Json) } -ThrottleLimit 2
        Assert (($results.awardedXp|Measure-Object -Sum).Sum -eq $q.reward) 'Concurrent reward duplicated.'
        Assert (@($results|Where-Object completedWorld).Count -eq 1) 'Concurrent completion event duplicated.'
    } else {
        $r=Post "/quests/$($q.id)/submit" @{answer=$q.answer}
        Assert ($r.awardedXp -eq $q.reward -and $r.progress.totalXp -eq $total) "Reward incorrect: $($q.id)"
    }
    $replay=Post "/quests/$($q.id)/submit" @{answer=$q.answer}
    Assert ($replay.awardedXp -eq 0 -and $replay.progress.totalXp -eq $total) 'Replay duplicated XP.'
}
$state=Get '/me/progress'
Assert ($state.totalXp -eq 2025 -and $state.javaCompleted -and $state.badges.Count -eq 5) 'Final Java state incorrect.'
Assert (@($state.worlds|Where-Object {-not $_.unlocked}).Count -eq 0) 'Java world remains locked.'
$map=Get '/campaigns/dsa/map';Assert ($map.unlocked) 'Normal Java completion did not unlock DSA.'
$topic=Get '/topics/arrays/progress';Assert ($topic.unlockedTrail -eq 1 -and $topic.completed -eq 0) 'Array Isles initial state incorrect.'
$first=Post '/questions/arrays-01/submit' @{answer='b'}
Assert ($first.awardedXp -eq 10 -and $first.progress.totalXp -eq 2035) 'Array Isles reward failed after Java journey.'
Post '/auth/logout' @{}|Out-Null
Post '/auth/login' @{email=$email;password=$password}|Out-Null
$saved=Get '/me/progress';$arrays=Get '/topics/arrays/progress'
Assert ($saved.totalXp -eq 2035 -and $saved.javaCompleted -and $saved.badges.Count -eq 5 -and $arrays.completed -eq 1) 'Logout/login lost progress.'
Write-Output 'PASS: normal registration -> all 16 Java quests -> 2025 XP/five badges -> DSA unlock -> Array Isles +10 XP -> login persistence; locks, wrong answers, replays and concurrent region finals verified.'
