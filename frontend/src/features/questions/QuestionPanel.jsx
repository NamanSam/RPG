import { useEffect, useRef, useState } from "react";
import { api } from "../../services/api";
import "../quests/quests.css";

export function QuestionPanel({ id, onClose, onResult }) {
  const dialog = useRef(null);
  const feedback = useRef(null);
  const [q, setQ] = useState(null);
  const [answer, setAnswer] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  useEffect(() => { const el = dialog.current; el.showModal(); return () => el.close(); }, []);
  useEffect(() => {
    let active = true;
    api(`/questions/${id}`).then(data => { if(active) setQ(data); }).catch(e => { if(active) setError(e.message); });
    return () => { active = false; };
  }, [id]);
  useEffect(() => { if(result) feedback.current?.focus(); }, [result]);
  async function submit(e) {
    e.preventDefault(); if(busy) return;
    setBusy(true); setError(""); setResult(null);
    try { const next = await api(`/questions/${id}/submit`, {method:"POST", body:JSON.stringify({answer})}); setResult(next); onResult(next); }
    catch(e) { setError(e.message); } finally { setBusy(false); }
  }
  const mode = q?.questionType;
  return <dialog ref={dialog} className="guide-dialog quest-dialog island-question" aria-labelledby="island-question-title" onCancel={e => busy ? e.preventDefault() : onClose()}>
    <button className="dialog-close" aria-label="Close question" disabled={busy} onClick={onClose}>×</button>
    {error && <p role="alert">{error}</p>}
    {!q ? <p id="island-question-title">Opening the explorer's journal…</p> : <>
      <p className="eyebrow">ARRAY ISLES · TRAIL {q.trail} · {q.difficulty}</p>
      <h2 id="island-question-title">{q.title}</h2>
      <p>{q.subtopic} · {q.xpReward} XP{q.isClassic ? " · Classic pattern" : ""}</p>
      <p>{q.description}</p>
      {q.starterCode && <pre><code>{q.starterCode}</code></pre>}
      <details><summary>Examples & answer format</summary>
        {q.examples.map((example,i) => <p key={i}><strong>Example:</strong> {example.input}<br/><strong>Result:</strong> {example.output}</p>)}
        <ul>{q.constraints.map(c=><li key={c}>{c}</li>)}</ul>
      </details>
      <form onSubmit={submit}>
        {mode === "MULTIPLE_CHOICE" ? <MultipleChoiceInput options={q.options} answer={answer} setAnswer={setAnswer} disabled={busy}/> :
          mode === "PREDICT_OUTPUT" ? <PredictOutputInput answer={answer} setAnswer={setAnswer} disabled={busy}/> :
          mode === "CODE_BLANK" ? <CodeBlankInput answer={answer} setAnswer={setAnswer} disabled={busy}/> :
          <StructuredAnswerInput answer={answer} setAnswer={setAnswer} disabled={busy} coding={mode === "CODING_CHALLENGE"}/>}
        <details><summary>Ask the island guide</summary>{q.hints.map(h=><p key={h}>{h}</p>)}</details>
        <button className="primary-button" disabled={busy || !answer.trim()}>{busy ? "Checking…" : result?.correct ? "Practice again →" : "Submit answer →"}</button>
      </form>
      {result && <div ref={feedback} tabIndex={-1} className={`quest-feedback ${result.correct ? "correct" : "incorrect"}`} role="status">
        <strong>{result.feedback}</strong><p>{result.explanation}</p>
        <span key={`${result.progress.totalXp}-${result.awardedXp}`} className={result.awardedXp ? "xp-gain" : ""}>+{result.awardedXp} XP</span>
        <p>Total XP: {result.progress.totalXp}</p>
        <button className="primary-button" onClick={onClose}>Return to the island →</button>
      </div>}
    </>}
  </dialog>;
}
function MultipleChoiceInput({options,answer,setAnswer,disabled}) {
  return <fieldset className="island-options" disabled={disabled}><legend>Choose your answer</legend>{options.map(o=><label key={o.id}><input type="radio" name="choice" value={o.id} checked={answer===o.id} onChange={()=>setAnswer(o.id)}/><code>{o.text}</code></label>)}</fieldset>;
}
function PredictOutputInput(props) { return <AnswerField {...props} label="Predicted output"/>; }
function CodeBlankInput(props) { return <AnswerField {...props} label="Code for the blank"/>; }
function StructuredAnswerInput({coding,...props}) { return <><p>{coding ? "Trace the algorithm and submit its result for this scenario." : "Submit the requested value or JSON structure."}</p><AnswerField {...props} label="Your answer"/></>; }
function AnswerField({answer,setAnswer,disabled,label}) { return <label className="island-answer">{label}<textarea value={answer} onChange={e=>setAnswer(e.target.value)} disabled={disabled} maxLength={2000} rows={3} required spellCheck={false}/></label>; }
