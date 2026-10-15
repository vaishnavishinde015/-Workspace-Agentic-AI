const form = document.querySelector("#chat-form");
const questionInput = document.querySelector("#question");
const sendButton = document.querySelector("#send-button");
const welcome = document.querySelector("#welcome");
const messageList = document.querySelector("#message-list");

const assistantIcon = `
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
        <path d="m10 2 1.6 5.2L17 9l-5.4 1.8L10 16l-1.6-5.2L3 9l5.4-1.8L10 2Z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/>
        <path d="m15.5 12 .7 2.1 2.1.7-2.1.7-.7 2.1-.7-2.1-2.1-.7 2.1-.7.7-2.1Z" fill="currentColor"/>
    </svg>`;

const userIcon = `
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
        <circle cx="10" cy="6.5" r="3" stroke="currentColor" stroke-width="1.4"/>
        <path d="M4.5 17c.4-3 2.3-4.5 5.5-4.5s5.1 1.5 5.5 4.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
    </svg>`;

function addMessage(role, content, pending = false) {
    const message = document.createElement("article");
    message.className = `message ${role}${pending ? " pending" : ""}`;

    const avatar = document.createElement("span");
    avatar.className = "message-avatar";
    avatar.setAttribute("aria-hidden", "true");
    avatar.innerHTML = role === "assistant" ? assistantIcon : userIcon;

    const body = document.createElement("div");
    body.className = "message-body";

    const label = document.createElement("div");
    label.className = "message-label";
    label.textContent = role === "assistant" ? "Assistant" : "You";

    const text = document.createElement("div");
    text.className = "message-content";
    text.textContent = content;

    body.append(label, text);
    message.append(avatar, body);
    messageList.append(message);
    message.scrollIntoView({ behavior: "smooth", block: "nearest" });

    return { message, text };
}

async function ask(question) {
    const trimmedQuestion = question.trim();
    if (!trimmedQuestion || sendButton.disabled) {
        return;
    }

    welcome.hidden = true;
    addMessage("user", trimmedQuestion);
    questionInput.value = "";
    questionInput.style.height = "auto";
    questionInput.disabled = true;
    sendButton.disabled = true;
    sendButton.querySelector("span").textContent = "Thinking";

    const pendingReply = addMessage("assistant", "Searching your knowledge base...", true);

    try {
        const response = await fetch("/api/chat", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ question: trimmedQuestion })
        });
        const payload = await response.json().catch(() => null);

        if (!response.ok) {
            throw new Error(payload?.error || `The request failed (${response.status}).`);
        }
        if (typeof payload?.answer !== "string" || !payload.answer.trim()) {
            throw new Error("The assistant returned an empty response. Please try again.");
        }

        pendingReply.text.textContent = payload.answer;
        pendingReply.message.classList.remove("pending");
    } catch (error) {
        pendingReply.text.textContent = error instanceof Error
            ? error.message
            : "Something went wrong. Please try again.";
        pendingReply.message.classList.remove("pending");
        pendingReply.message.classList.add("error");
    } finally {
        questionInput.disabled = false;
        sendButton.disabled = false;
        sendButton.querySelector("span").textContent = "Ask";
        questionInput.focus();
    }
}

form.addEventListener("submit", (event) => {
    event.preventDefault();
    ask(questionInput.value);
});

questionInput.addEventListener("input", () => {
    questionInput.style.height = "auto";
    questionInput.style.height = `${Math.min(questionInput.scrollHeight, 160)}px`;
});

questionInput.addEventListener("keydown", (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
        form.requestSubmit();
    }
});

document.querySelectorAll("[data-prompt]").forEach((button) => {
    button.addEventListener("click", () => ask(button.dataset.prompt));
});
