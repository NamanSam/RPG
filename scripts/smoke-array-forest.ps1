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
}
$quests=@(
    @{id='first-variable';answer='5';reward=50}, @{id='choose-the-type';answer='boolean';reward=50}, @{id='operator-training';answer='2';reward=75},
    @{id='the-gatekeeper';answer='coins >= 10';reward=75}, @{id='the-looping-mill';answer='for (int i = 0; i < 3; i++)';reward=100}, @{id='the-endless-well';answer='8';reward=125},
    @{id='the-lost-index';answer='9';reward=100}, @{id='forest-traversal';answer='10';reward=125}, @{id='hidden-maximum';answer='-3';reward=150}
)
$email="forest-smoke-$([Guid]::NewGuid().ToString('N'))@codequest.test";$password=[Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(16))
$new=Post '/auth/register' @{email=$email;password=$password;displayName='Forest Explorer'};Assert ($new.totalXp -eq 0) 'Starting XP is not zero.'
Locked 'the-lost-index' '9';$total=0
for($i=0;$i -lt $quests.Count;$i++) {
    $q=$quests[$i];if($i -lt $quests.Count-1){Locked $quests[$i+1].id $quests[$i+1].answer}
    $bad=Post "/quests/$($q.id)/submit" @{answer='wrong'};Assert ($bad.awardedXp -eq 0 -and $bad.progress.totalXp -eq $total) 'Wrong answer changed XP.'
    $total+=$q.reward;$r=Post "/quests/$($q.id)/submit" @{answer=$q.answer};Assert ($r.awardedXp -eq $q.reward -and $r.progress.totalXp -eq $total) "Reward incorrect: $($q.id)"
    $replay=Post "/quests/$($q.id)/submit" @{answer=$q.answer};Assert ($replay.awardedXp -eq 0 -and $replay.progress.totalXp -eq $total) 'Replay duplicated XP.'
}
$state=Get '/me/progress';Assert ($state.totalXp -eq 850 -and $state.forestCompleted -eq 3) 'Final Forest progress incorrect.'
Assert (@($state.badges|Where-Object id -eq 'array-forest').Count -eq 1) 'Forest badge missing or duplicated.'
Assert (($state.worlds|Where-Object slug -eq 'oop-canyon').unlocked) 'OOP Canyon did not unlock.'
Post '/auth/logout' @{}|Out-Null;Post '/auth/login' @{email=$email;password=$password}|Out-Null
$saved=Get '/me/progress';Assert ($saved.totalXp -eq 850 -and $saved.forestCompleted -eq 3) 'Logout/login lost Forest progress.'
Write-Output 'PASS: fresh registration -> Beach -> Village -> Forest -> 850 XP; locks, wrong answers, replay prevention, badge, OOP unlock, and login persistence verified.'
