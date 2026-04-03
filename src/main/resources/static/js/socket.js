let stompClient = null;
let currentChatRoomId = null;
let chatMessages = [];

function connectWebSocket(onConnectedCallback) {
    if (stompClient && stompClient.connected) {
        if(onConnectedCallback) onConnectedCallback();
        return;
    }
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Disable debug logs

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        if(onConnectedCallback) onConnectedCallback();
    }, function(error) {
        console.error('STOMP connection error:', error);
        setTimeout(() => connectWebSocket(onConnectedCallback), 5000);
    });
}

// ----------------------------------------------------
// Customer Chat Logic (main.html)
// ----------------------------------------------------
function toggleChat() {
    const w = document.getElementById('chatWindow');
    if (w.classList.contains('d-none')) {
        w.classList.remove('d-none');
        initCustomerChat();
    } else {
        w.classList.add('d-none');
    }
}

function initCustomerChat() {
    let sName = 'Khách';
    const nameElem = document.getElementById('currentUsername');
    if (nameElem && nameElem.innerText.trim() !== '') { 
        sName = nameElem.innerText.trim(); 
    }

    // Use name as part of the local storage key so different users on the same browser have separate chat rooms
    const suffix = sName === 'Khách' ? 'guest' : sName.replace(/\s+/g, '-').toLowerCase();
    const storageKey = 'chatSessionId_' + suffix;

    // Generate simple session ID if not exists
    if (!localStorage.getItem(storageKey)) {
        localStorage.setItem(storageKey, 'CUST-' + suffix + '-' + Math.random().toString(36).substr(2, 9));
    }
    currentChatRoomId = localStorage.getItem(storageKey);

    connectWebSocket(() => {
        // Subscribe to messages in this room
        stompClient.subscribe('/topic/messages/' + currentChatRoomId, function (message) {
            const msg = JSON.parse(message.body);
            appendChatMessage(msg);
        });

        // Load history
        fetch('/api/chat/history/' + currentChatRoomId)
            .then(res => res.json())
            .then(data => {
                document.getElementById('chatBody').innerHTML = '';
                data.forEach(appendChatMessage);
                scrollToBottom('chatBody');
                setTimeout(() => scrollToBottom('chatBody'), 500); // Image waiting period
            });
    });
}

let pendingCustomerImage = null;

function clearCustomerPreview() {
    pendingCustomerImage = null;
    document.getElementById('customerImgPreview').style.display = 'none';
    document.getElementById('chatImageInput').value = '';
}

function handleCustomerImageSelection(file) {
    if (!file || !file.type.startsWith('image/')) return;
    pendingCustomerImage = file;
    const url = URL.createObjectURL(file);
    document.getElementById('customerImgPreviewEl').src = url;
    document.getElementById('customerImgPreview').style.display = 'block';
}

function sendCustomerChat() {
    const input = document.getElementById('chatInput');
    const content = input.value.trim();
    
    if (!content && !pendingCustomerImage) return;
    if (!stompClient) return;

    let sName = 'Khách';
    const nameElem = document.getElementById('currentUsername');
    if (nameElem) { sName = nameElem.innerText.trim(); }

    // If there is a pending image, upload it first
    if (pendingCustomerImage) {
        const formData = new FormData();
        formData.append('imageFile', pendingCustomerImage);

        fetch('/api/chat/upload', {
            method: 'POST',
            body: formData
        })
        .then(r => r.text())
        .then(url => {
            if(url !== 'error') {
                const imgMsg = {
                    roomId: currentChatRoomId,
                    senderType: 'CUSTOMER',
                    senderName: sName,
                    messageType: 'IMAGE',
                    content: url
                };
                stompClient.send("/app/chat.send", {}, JSON.stringify(imgMsg));
                
                // Then send text if exists
                if (content) {
                    const txtMsg = {
                        roomId: currentChatRoomId,
                        senderType: 'CUSTOMER',
                        senderName: sName,
                        messageType: 'TEXT',
                        content: content
                    };
                    // Delay slightly to ensure order
                    setTimeout(() => {
                        stompClient.send("/app/chat.send", {}, JSON.stringify(txtMsg));
                    }, 200);
                }
            } else {
                alert('Lỗi khi tải ảnh lên!');
            }
        })
        .catch(e => console.error(e))
        .finally(() => {
            clearCustomerPreview();
            input.value = '';
        });

    } else {
        // Only text
        const msg = {
            roomId: currentChatRoomId,
            senderType: 'CUSTOMER',
            senderName: sName,
            messageType: 'TEXT',
            content: content
        };
        stompClient.send("/app/chat.send", {}, JSON.stringify(msg));
        input.value = '';
    }
}

// Hook events when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Customer File Input Change
    const chatImageInput = document.getElementById('chatImageInput');
    if (chatImageInput) {
        chatImageInput.addEventListener('change', (e) => {
            if (e.target.files.length > 0) {
                handleCustomerImageSelection(e.target.files[0]);
            }
        });
    }

    // Customer Input Paste
    const chatInput = document.getElementById('chatInput');
    if (chatInput) {
        chatInput.addEventListener('paste', (e) => {
            const items = (e.clipboardData || e.originalEvent.clipboardData).items;
            for (let index in items) {
                const item = items[index];
                if (item.kind === 'file' && item.type.startsWith('image/')) {
                    const blob = item.getAsFile();
                    handleCustomerImageSelection(blob);
                    e.preventDefault(); // Stop pasting image as text
                }
            }
        });
    }
});

function appendChatMessage(msg) {
    const body = document.getElementById('chatBody');
    if(!body) return; // Not on customer page
    const isCustomer = msg.senderType === 'CUSTOMER';
    
    const div = document.createElement('div');
    div.style.padding = '8px 12px';
    div.style.borderRadius = '12px';
    div.style.maxWidth = '85%';
    div.style.marginBottom = '10px';
    div.style.wordBreak = 'break-word';
    div.style.border = '1px solid var(--border)';

    if (isCustomer) {
        div.style.alignSelf = 'flex-end';
        div.style.background = 'var(--clr-primary)';
        div.style.color = '#fff';
        div.style.borderTopRightRadius = '0';
    } else {
        div.style.alignSelf = 'flex-start';
        div.style.background = 'var(--bg-input)';
        div.style.color = 'var(--txt)';
        div.style.borderTopLeftRadius = '0';
    }

    if (msg.messageType === 'IMAGE') {
        div.style.padding = '5px'; // less padding for images
        div.style.background = 'none';
        div.style.border = 'none';
        div.innerHTML = `<img src="${msg.content}" onload="scrollToBottom('chatBody')" style="max-width: 100%; border-radius: 8px; cursor: pointer;" onclick="window.open('${msg.content}', '_blank')">`;
    } else {
        div.innerText = msg.content;
    }
    
    body.appendChild(div);
    scrollToBottom('chatBody');
}

function scrollToBottom(id) {
    const e = document.getElementById(id);
    if(e) e.scrollTop = e.scrollHeight;
}
