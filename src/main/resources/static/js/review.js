window.addEventListener('DOMContentLoaded', () => {

    const editorElement = document.querySelector('#editor');
    const uploadedImages = [];
    let quill;

    if (editorElement) {
        quill = new Quill('#editor', {
            theme: 'snow',
            placeholder: '1. 사용한 공부 방법 / 전략 \n2. 효과 / 결과',
            modules: {
                toolbar: {
                    container: [
                        ['bold', 'italic', 'underline', 'strike'],
                        ['image', 'link'],
                        [{'list': 'ordered'}, {'list': 'bullet'}]
                    ],
                    handlers: {
                        image: imageHandler
                    }
                }
            }
        });
    }

    async function imageHandler() {
        const input = document.createElement('input');
        input.setAttribute('type', 'file');
        input.setAttribute('accept', 'image/*');
        input.click();

        input.onchange = async () => {
            const file = input.files[0];
            if (!file) {
                return;
            }

            const formData = new FormData();
            formData.append('file', file);

            try {
                const res = await fetch('/api/images/upload', {
                    method: 'POST',
                    body: formData
                });

                const {imageUrl} = await res.json();

                const filename = imageUrl.split('/').pop();

                uploadedImages.push({
                    filename: filename,
                    filePath: imageUrl
                });

                const range = quill.getSelection(true);
                quill.insertText(range.index, '\n'); // 줄바꿈
                quill.insertEmbed(range.index + 1, 'image', imageUrl);
                quill.setSelection(range.index + 2);

            } catch (err) {
                console.error('이미지 업로드 실패:', err);
            }
        };
    }

    const type = document.body.dataset.type;
    let itemId;

    if (type === 'review') {
        itemId = document.body.dataset.reviewId;
    } else if (type === 'contact') {
        itemId = document.body.dataset.contactId;
    }

    const isEditMode = itemId && itemId !== 'null';

    if (isEditMode) {
        fetch(`/api/${type}s/${itemId}`)
        .then(res => {
            if (!res.ok) {
                throw new Error(`${type} 불러오기 실패`);
            }
            return res.json();
        })
        .then(item => {
            const titleInput = document.getElementById('title');
            if (titleInput) {
                titleInput.value = item.title || '';
            }
            if (quill) {
                quill.root.innerHTML = item.content || '';
            }
        })
        .catch(() => alert(`${type} 로딩 실패`));
    }

// 저장 버튼 클릭
    const submitBtn = document.getElementById('submitBtn');
    if (submitBtn) {
        submitBtn.addEventListener('click', async () => {

            const endpoints = {
                review: '/api/reviews',
                contact: '/api/contacts',
            };

            const endpoint = isEditMode ? `${endpoints[type]}/${itemId}`
                : endpoints[type];
            const method = isEditMode ? 'PUT' : 'POST';

            const payload = {
                title: document.getElementById('title').value,
                content: quill.root.innerHTML,
                images: uploadedImages,
            };

            try {
                const response = await fetch(endpoint, {
                    method,
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(payload),
                });

                if (!response.ok) {
                    const errText = await response.text();
                    throw new Error(`HTTP ${response.status} - ${errText}`);
                }

                alert(isEditMode ? '수정이 완료되었습니다!' : '등록이 완료되었습니다!');
                window.location.href = isEditMode ? `/${type}s/${itemId}`
                    : `/${type}s`;

            } catch (error) {
                console.error('업로드 실패:', error.message);
                alert(isEditMode ? '수정에 실패했습니다. 다시 시도해주세요.'
                    : '등록에 실패했습니다. 다시 시도해주세요.');
            }
        });
    }

    const deleteBtn = document.getElementById('deleteBtn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', async () => {
            const type = document.body.dataset.type;
            const itemId = document.body.dataset[`${type}Id`];

            if (!itemId || itemId === 'null') {
                alert('삭제할 항목 ID가 없습니다.');
                return;
            }

            const confirmDelete = confirm('정말 삭제하시겠습니까?');
            if (!confirmDelete) {
                return;
            }

            try {
                const response = await fetch(`/api/${type}s/${itemId}`, {
                    method: 'DELETE'
                });

                if (!response.ok) {
                    const errText = await response.text();
                    throw new Error(`삭제 실패: ${errText}`);
                }

                alert('삭제가 완료되었습니다.');
                window.location.href = `/${type}s`;
            } catch (err) {
                console.error('삭제 중 오류:', err);
                alert('삭제에 실패했습니다. 다시 시도해주세요.');
            }
        });
    }
});



