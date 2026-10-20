# PowerShell 7. Uses normal registration and quest APIs; no database grants or demo fixture.
param([string]$BaseUrl='http://127.0.0.1:8085',[switch]$PrepareBrowser)
$ErrorActionPreference='Stop'
function Assert($condition,$message){if(-not $condition){throw $message}}
$session=New-Object Microsoft.PowerShell.Commands.WebRequestSession
function Headers {$c=Invoke-RestMethod "$BaseUrl/api/auth/csrf" -WebSession $session;$h=@{};$h[$c.headerName]=$c.token;return $h}
function Post($path,$body){Invoke-RestMethod "$BaseUrl/api$path" -Method Post -ContentType 'application/json' -WebSession $session -Headers (Headers) -Body ($body|ConvertTo-Json)}
function Get($path){Invoke-RestMethod "$BaseUrl/api$path" -WebSession $session}
function Locked($id){$r=Invoke-WebRequest "$BaseUrl/api/quests/$id/submit" -Method Post -ContentType 'application/json' -WebSession $session -Headers (Headers) -Body '{"answer":"3"}' -SkipHttpErrorCheck;Assert ($r.StatusCode -eq 403) "Locked submission accepted: $id"}
$quests=@(
 @{id='first-variable';answer='5';reward=50},@{id='choose-the-type';answer='boolean';reward=50},@{id='operator-training';answer='2';reward=75},
 @{id='the-gatekeeper';answer='coins >= 10';reward=75},@{id='the-looping-mill';answer='for (int i = 0; i < 3; i++)';reward=100},@{id='the-endless-well';answer='8';reward=125},
 @{id='the-lost-index';answer='9';reward=100},@{id='forest-traversal';answer='10';reward=125},@{id='hidden-maximum';answer='-3';reward=150},
 @{id='blueprint-maker';answer='Bridge bridge = new Bridge();';reward=125},@{id='the-hidden-vault';answer='private';reward=150},@{id='bloodline-of-classes';answer='extends';reward=175},@{id='many-forms';answer='Rope';reward=200},
 @{id='the-dynamic-scroll';answer='3';reward=150},@{id='the-key-keeper';answer='7';reward=175},@{id='hall-of-uniques';answer='3';reward=200}
)
$email="kingdom-smoke-$([Guid]::NewGuid().ToString('N'))@codequest.test"
$password='Kingdom-local-test-2026!'
Post '/auth/register' @{email=$email;password=$password;displayName='Royal Scholar'}|Out-Null
$total=0
for($i=0;$i -lt $quests.Count;$i++){
 if($PrepareBrowser -and $i -eq 13){Write-Output "Browser account: $email | $password | 1500 XP, no fixture used";return}
 $q=$quests[$i]
 if($i -lt $quests.Count-1){Locked $quests[$i+1].id}
 $map=Get '/campaigns/dsa/map';Assert (-not $map.unlocked) 'DSA opened before final quest.'
 $bad=Post "/quests/$($q.id)/submit" @{answer='wrong'};Assert ($bad.awardedXp -eq 0 -and $bad.progress.totalXp -eq $total) 'Wrong answer changed XP.'
 $total+=$q.reward;$r=Post "/quests/$($q.id)/submit" @{answer=$q.answer};Assert ($r.awardedXp -eq $q.reward -and $r.progress.totalXp -eq $total) "Wrong reward: $($q.id)"
 $replay=Post "/quests/$($q.id)/submit" @{answer=$q.answer};Assert ($replay.awardedXp -eq 0 -and $replay.progress.totalXp -eq $total) 'Replay duplicated XP.'
}
$p=Get '/me/progress';Assert ($p.totalXp -eq 2025 -and $p.javaXp -eq 2025 -and $p.javaCampaignComplete -and $p.kingdomCompleted -eq 3) 'Java completion incorrect.'
Assert (@($p.badges).Count -eq 5) 'Expected five Java badges.'
Assert ((Get '/campaigns/dsa/map').unlocked) 'DSA remains locked.'
$a=Get '/topics/arrays/progress';Assert ($a.completed -eq 0 -and $a.unlockedTrail -eq 1) 'Normal Array Isles enrollment incorrect.'
Get '/questions/arrays-01'|Out-Null
Post '/auth/logout' @{}|Out-Null;Post '/auth/login' @{email=$email;password=$password}|Out-Null
$p=Get '/me/progress';Assert ($p.javaCampaignComplete -and $p.javaXp -eq 2025) 'Login lost campaign state.'
Assert ((Get '/topics/arrays/progress').unlockedTrail -eq 1) 'Login lost normal Array Isles access.'
Write-Output 'PASS: normal registration -> all 16 Java quests -> 2025 Java XP -> five badges -> Java complete -> DSA -> Array Isles. Wrong/replay/lock checks and login persistence passed.'
