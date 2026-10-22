param([string]$BaseUrl='http://127.0.0.1:8085')
$ErrorActionPreference='Stop';function Assert($ok,$message){if(-not $ok){throw $message}}
$session=New-Object Microsoft.PowerShell.Commands.WebRequestSession
function Headers{$t=Invoke-RestMethod "$BaseUrl/api/auth/csrf" -WebSession $session;$h=@{};$h[$t.headerName]=$t.token;return $h}
function Post($path,$body){Invoke-RestMethod "$BaseUrl/api$path" -Method Post -ContentType 'application/json' -WebSession $session -Headers (Headers) -Body ($body|ConvertTo-Json)}
function Get($path){Invoke-RestMethod "$BaseUrl/api$path" -WebSession $session}
function Locked($path){try{Invoke-WebRequest "$BaseUrl/api$path" -WebSession $session|Out-Null;return $false}catch [System.Net.WebException]{return [int]$_.Exception.Response.StatusCode -eq 403}}
$email="graph-smoke-$([Guid]::NewGuid().ToString('N'))@codequest.test";$password='Graph-mountains-test-2026!';Post '/auth/register' @{email=$email;password=$password;displayName='Graph Mountains Oracle'}|Out-Null
Assert (Locked '/questions/graphs-01') 'Graph Mountains opened before BST Valley completion.'
$story=@(@{id='first-variable';a='5'},@{id='choose-the-type';a='boolean'},@{id='operator-training';a='2'},@{id='the-gatekeeper';a='coins >= 10'},@{id='the-looping-mill';a='for (int i = 0; i < 3; i++)'},@{id='the-endless-well';a='8'},@{id='the-lost-index';a='9'},@{id='forest-traversal';a='10'},@{id='hidden-maximum';a='-3'},@{id='blueprint-maker';a='Bridge bridge = new Bridge();'},@{id='the-hidden-vault';a='private'},@{id='bloodline-of-classes';a='extends'},@{id='many-forms';a='Rope'},@{id='the-dynamic-scroll';a='3'},@{id='the-key-keeper';a='7'},@{id='hall-of-uniques';a='3'})
foreach($q in $story){Post "/quests/$($q.id)/submit" @{answer=$q.a}|Out-Null}
foreach($file in @('../content/dsa/arrays/v1.json','../content/dsa/strings/v1.json','../content/dsa/linked-lists/v1.json','../content/dsa/stacks/v1.json','../content/dsa/queues/v1.json','../content/dsa/trees/v1.json','../content/dsa/binary-search-trees/v1.json')){$prior=Get-Content (Join-Path $PSScriptRoot $file) -Raw|ConvertFrom-Json;foreach($q in $prior.questions){Post "/questions/$($q.id)/submit" @{answer=$q.expectedAnswer}|Out-Null}}
$start=Get '/topics/graphs/progress';Assert ($start.completed -eq 0 -and $start.unlockedTrail -eq 1 -and $start.totalXp -eq 8325) 'Graph Mountains did not unlock correctly.'
$pack=Get-Content (Join-Path $PSScriptRoot '../content/dsa/graphs/v1.json') -Raw|ConvertFrom-Json
foreach($q in $pack.questions){
 if($q.order%10 -ne 0){Assert (Locked "/questions/$($pack.questions[$q.order].id)") "Question lock bypassed after $($q.id)"}
 $before=Get '/topics/graphs/progress';$bad=Post "/questions/$($q.id)/submit" @{answer='not-an-answer'};Assert ($bad.awardedXp -eq 0 -and $bad.progress.totalXp -eq $before.totalXp) 'Wrong answer changed XP.'
 $right=Post "/questions/$($q.id)/submit" @{answer=$q.expectedAnswer};Assert ($right.awardedXp -eq $q.xpReward) "Wrong reward for $($q.id)"
 $again=Post "/questions/$($q.id)/submit" @{answer=$q.expectedAnswer};Assert ($again.awardedXp -eq 0) 'Replay duplicated XP.'
}
$done=Get '/topics/graphs/progress';Assert ($done.completed -eq 50 -and $done.totalXp -eq 9225 -and $done.badge -eq 'Graph Mountains Oracle') 'Final mountain state is incorrect.'
Post '/auth/logout' @{}|Out-Null;Post '/auth/login' @{email=$email;password=$password}|Out-Null
$saved=Get '/topics/graphs/progress';Assert ($saved.completed -eq 50 -and $saved.totalXp -eq 9225 -and $saved.badge -eq $done.badge) 'Mountain progress did not persist.'
Write-Output 'PASS: BST gate, 50 Graph questions, sequential locks, 900 XP, badge, and login persistence.'





