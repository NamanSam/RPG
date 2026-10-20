# Uses normal accounts, public APIs and the checked-in pack. No fixture or direct progress grants.
param([string]$BaseUrl='http://127.0.0.1:8085',[ValidateRange(0,50)][int]$StopAfter=50,[string]$Email='')
$ErrorActionPreference='Stop'
function Assert($ok,$message){if(-not $ok){throw $message}}
$session=New-Object Microsoft.PowerShell.Commands.WebRequestSession
function Headers{$t=Invoke-RestMethod "$BaseUrl/api/auth/csrf" -WebSession $session;$h=@{};$h[$t.headerName]=$t.token;return $h}
function Post($path,$body){Invoke-RestMethod "$BaseUrl/api$path" -Method Post -ContentType 'application/json' -WebSession $session -Headers (Headers) -Body ($body|ConvertTo-Json)}
function Get($path){Invoke-RestMethod "$BaseUrl/api$path" -WebSession $session}
$password='Practice-local-test-2026!'
if($Email){
 if($Email -notmatch '^practice-smoke-[a-f0-9]{32}@codequest\.test$'){throw 'Only synthetic practice smoke accounts can be resumed.'}
 Post '/auth/login' @{email=$Email;password=$password}|Out-Null
}else{
 $Email="practice-smoke-$([Guid]::NewGuid().ToString('N'))@codequest.test"
 Post '/auth/register' @{email=$Email;password=$password;displayName='Practice Explorer'}|Out-Null
 $p=Get '/topics/beach-practice/progress';Assert (@($p.trails|Where-Object unlocked).Count -eq 0) 'Practice opened before story.'
 foreach($q in @(@{id='first-variable';answer='5'},@{id='choose-the-type';answer='boolean'},@{id='operator-training';answer='2'})){Post "/quests/$($q.id)/submit" @{answer=$q.answer}|Out-Null}
}
$pack=Get-Content (Join-Path $PSScriptRoot '../content/java/beginner-beach/v1.json') -Raw|ConvertFrom-Json
Assert ($pack.questions.Count -eq 50) 'Expected fifty questions.'
for($i=0;$i -lt $StopAfter;$i++){
 $q=$pack.questions[$i];$before=Get '/topics/beach-practice/progress';$marker=@($before.trails.questions|Where-Object id -eq $q.id)[0]
 if($marker.status -ne 'COMPLETED' -and $q.order%10 -ne 0){
  $id=$pack.questions[$i+1].id
  $locked=Invoke-WebRequest "$BaseUrl/api/questions/$id/submit" -Method Post -ContentType 'application/json' -WebSession $session -Headers (Headers) -Body '{"answer":"11"}' -SkipHttpErrorCheck
  Assert ($locked.StatusCode -eq 403) "Sequential lock bypassed: $id"
 }
 $bad=Post "/questions/$($q.id)/submit" @{answer='not-an-answer'};Assert ($bad.awardedXp -eq 0 -and $bad.progress.practiceXp -eq $before.practiceXp) 'Wrong answer changed XP.'
 $r=Post "/questions/$($q.id)/submit" @{answer=$q.expectedAnswer};$expected=if($marker.status -eq 'COMPLETED'){0}else{$q.xpReward}
 Assert ($r.awardedXp -eq $expected -and $r.progress.practiceXp -eq $before.practiceXp+$expected -and $r.progress.totalXp -eq 175) "Incorrect separated reward: $($q.id)"
 $replay=Post "/questions/$($q.id)/submit" @{answer=$q.expectedAnswer};Assert ($replay.awardedXp -eq 0 -and $replay.progress.practiceXp -eq $r.progress.practiceXp) 'Replay duplicated practice XP.'
}
$p=Get '/topics/beach-practice/progress'
if($StopAfter -eq 50){Assert ($p.completed -eq 50 -and $p.practiceXp -eq 900 -and $p.badge -eq 'Beginner Beach Mastery') 'Mastery state incorrect.'}
Post '/auth/logout' @{}|Out-Null;Post '/auth/login' @{email=$Email;password=$password}|Out-Null
$saved=Get '/topics/beach-practice/progress';Assert ($saved.completed -eq $p.completed -and $saved.practiceXp -eq $p.practiceXp) 'Login lost practice state.'
$story=Get '/me/progress';Assert ($story.totalXp -eq 175 -and $story.javaXp -eq 175 -and ($story.worlds|Where-Object slug -eq 'loop-village').unlocked) 'Practice changed story XP or region unlocks.'
Write-Output "PASS: $($p.completed)/50 practice, $($p.practiceXp) separate practice XP, 175 story XP, login persistence. Account: $Email | $password"
