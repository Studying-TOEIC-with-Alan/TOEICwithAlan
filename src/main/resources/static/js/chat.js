const params = new URLSearchParams(window.location.search);
const roomId = params.get('roomId');
let myNickname = null;

function fetchMyInfo() {
    return fetch('/api/chat/users/me')
    .then(res => res.json())
    .then(data => {
        myNickname = data.nickname;
    });
}

function createMessageElement(msg) {
    const wrapper = document.createElement('div');
    wrapper.classList.add('message-wrapper');
    wrapper.classList.add(msg.nickname === myNickname ? 'my-message-wrapper'
        : 'other-message-wrapper');

    const nicknameDiv = document.createElement('div');
    nicknameDiv.classList.add('nickname');
    nicknameDiv.textContent = msg.nickname;
    wrapper.appendChild(nicknameDiv);

    const messageDiv = document.createElement('div');
    messageDiv.classList.add('message');
    messageDiv.classList.add(
        msg.nickname === myNickname ? 'my-message' : 'other-message');

    const contentDiv = document.createElement('div');
    contentDiv.classList.add('message-content');
    contentDiv.textContent = msg.message;

    const timeDiv = document.createElement('div');
    timeDiv.classList.add('timestamp');
    timeDiv.textContent = msg.sentAt;

    messageDiv.appendChild(contentDiv);

    wrapper.appendChild(messageDiv);
    wrapper.appendChild(timeDiv);
    return wrapper;
}

function renderMessages(messages) {
    const box = document.getElementById('chat-box');
    box.innerHTML = '';
    messages.forEach(msg => {
        const msgElem = createMessageElement(msg);
        box.appendChild(msgElem);
    });
    box.scrollTop = box.scrollHeight;
}

function fetchMessages() {
    fetch(`/api/chat/all?roomId=${roomId}`)
    .then(res => res.json())
    .then(renderMessages);
}

function sendMessage() {
    const input = document.getElementById('chat-input');
    const message = input.value.trim();
    if (message === '') {
        return;
    }

    fetch(`/api/chat/send?roomId=${roomId}`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(message)
    }).then(() => {
        input.value = '';
        fetchMessages();
    });
}

function longPoll() {
    fetch(`/api/chat/receive?roomId=${roomId}`)
    .then(res => res.json())
    .then(messages => {
        if (messages.length > 0) {
            const box = document.getElementById('chat-box');
            messages.forEach(msg => {
                const msgElem = createMessageElement(msg);
                box.appendChild(msgElem);
            });
            box.scrollTop = box.scrollHeight;
        }
        longPoll();
    })
    .catch(() => setTimeout(longPoll, 1000));
}

fetchMyInfo().then(() => {
    fetchMessages();
    longPoll();
});
