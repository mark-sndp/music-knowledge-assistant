const form = document.querySelector("#query-form");
const question = document.querySelector("#question");
const askButton = document.querySelector("#ask-button");
const charCount = document.querySelector("#char-count");
const message = document.querySelector("#message");
const answerSection = document.querySelector("#answer-section");
const answerText = document.querySelector("#answer-text");
const sourceCount = document.querySelector("#source-count");
const sources = document.querySelector("#sources");

question.addEventListener("input", () => {
  charCount.textContent = `${question.value.length} / 1000`;
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  const value = question.value.trim();
  if (!value) {
    showMessage("Enter a question to search the archive.");
    return;
  }

  setLoading(true);
  showMessage("");
  answerSection.hidden = true;
  try {
    const response = await fetch("http://localhost:8080/api/v1/query", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ question: value })
    });
    if (!response.ok) {
      throw new Error(response.status === 400 ? "Please enter a valid question." : "The archive is temporarily unavailable.");
    }
    renderResult(await response.json());
  } catch (error) {
    showMessage(error.message || "The archive is temporarily unavailable.");
  } finally {
    setLoading(false);
  }
});

function renderResult(result) {
  answerText.textContent = result.answer;
  const resultSources = result.sources || [];
  sourceCount.textContent = `${resultSources.length} source${resultSources.length === 1 ? "" : "s"}`;
  sources.replaceChildren(...resultSources.map(renderSource));
  answerSection.hidden = false;
}

function renderSource(source) {
  const card = document.createElement("article");
  card.className = "source";
  const title = document.createElement("h3");
  title.className = "source-title";
  title.textContent = source.title;
  const content = document.createElement("p");
  content.className = "source-content";
  content.textContent = source.content;
  const link = document.createElement("a");
  link.href = source.uri;
  link.target = "_blank";
  link.rel = "noreferrer";
  link.textContent = "Open source ->";
  const score = document.createElement("span");
  score.className = "source-score";
  score.textContent = `${Math.round(source.score * 100)}% match`;
  card.append(title, content, link, score);
  return card;
}

function setLoading(loading) {
  askButton.disabled = loading;
  askButton.querySelector("span:first-child").textContent = loading ? "Searching archive" : "Ask the archive";
}

function showMessage(text) {
  message.textContent = text;
}
