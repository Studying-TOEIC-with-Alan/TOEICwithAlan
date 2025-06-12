window.addEventListener('DOMContentLoaded', () => {
    const baseUrl = 'https://myprojectb12.s3.ap-northeast-2.amazonaws.com/';
    const type = document.body.dataset.type;
    let itemId = document.body.dataset[`${type}Id`];
    const imgTagsInContent = document.querySelectorAll('#content img');
    imgTagsInContent.forEach(img => {
        const src = img.getAttribute('src');
        if (src && src.startsWith('upload/')) {
            img.src = baseUrl + src;
        }
    });

    const editBtn = document.getElementById('editBtn');

    if (editBtn) {
        editBtn.addEventListener('click', () => {
            if (!itemId) {
                alert(`수정할 ${type === 'review' ? '리뷰' : '문의'} ID가 없습니다.`);
                return;
            }
            window.location.href = `/${type}s/${itemId}/edit`;
        });
    }

    const backBtn = document.getElementById('backBtn');
    if (backBtn && type) {
        backBtn.addEventListener('click', () => {
            if (type === 'review') {
                window.location.href = '/reviews';
            } else if (type === 'contact') {
                window.location.href = '/contacts';
            }
        });
    }

    const editorElement = document.querySelector('#editor');
    const uploadedItems = [];
    const uploadedFilePaths = [];

    let quill;
    if (editorElement) {
        const editorType = editorElement.dataset.type;
        const placeholderText = editorType === 'review'
            ? '1. 사용한 공부 방법 / 전략 \n2. 효과 / 결과'
            : '1. 문의 내용을 자세히 적어주세요 \n2. 연락처를 남겨주세요';
        quill = new Quill('#editor', {
            theme: 'snow',
            placeholder: placeholderText,
            modules: {
                toolbar: {
                    container: [
                        ['bold', 'italic', 'underline', 'strike'],
                        ['image', 'file'],
                        [{'list': 'ordered'}, {'list': 'bullet'}]
                    ],
                    handlers: {
                        image: imageHandler,
                        file: fileHandler
                    }
                }
            }
        });
        const toolbar = document.querySelector('.ql-toolbar .ql-formats');
        const fileButton = document.createElement('button');
        fileButton.setAttribute('type', 'button');
        fileButton.classList.add('ql-file');
        fileButton.innerHTML = '<strong style="filter: grayscale(100%)">🗂️</strong>';
        fileButton.style.fontSize = '12px';
        fileButton.addEventListener('click', fileHandler);
        if (toolbar) {
            toolbar.appendChild(fileButton);
        }
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

            const reader = new FileReader();
            reader.onload = (e) => {
                const base64 = e.target.result;
                const range = quill.getSelection(true);
                quill.insertText(range.index, '\n');
                quill.insertEmbed(range.index + 1, 'image', base64);
                quill.insertText(range.index + 2, '\n');
                quill.setSelection(range.index + 3);
            };
            reader.readAsDataURL(file);
        };
    }

    async function fileHandler() {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.zip,.hwp';
        input.click();

        input.onchange = async () => {
            const file = input.files[0];
            if (!file) {
                return;
            }

            const formData = new FormData();
            formData.append('file', file);

            const res = await fetch('/api/files/upload', {
                method: 'POST',
                body: formData
            });

            if (!res.ok) {
                alert('파일 업로드 실패');
                return;
            }

            const {filePath} = await res.json();
            uploadedFilePaths.push(filePath);
            const fileUrl = baseUrl + filePath;
            const fileName = file.name;

            const range = quill.getSelection(true);
            quill.insertText(range.index, fileName, 'link', fileUrl);
            quill.insertText(range.index + fileName.length, '\n');

            uploadedItems.push({
                filename: filePath.split('/').pop(),
                filePath: filePath
            });
        };
    }

    if (type === 'review') {
        itemId = document.body.dataset.reviewId;
    } else if (type === 'contact') {
        itemId = document.body.dataset.contactId;
    }

    const isEditMode = itemId && itemId !== 'null';
    let initialFilePaths = [];
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

            initialFilePaths = (item.images || []).map(img => img.filePath);
        })
        .catch(() => alert(`${type} 로딩 실패`));
    }

    const submitBtn = document.getElementById('submitBtn');
    if (submitBtn) {
        submitBtn.addEventListener('click', async () => {
            const title = document.getElementById('title').value.trim();
            const contentText = quill.getText().trim();

            if (!title) {
                alert('제목을 입력해주세요.');
                return;
            }

            if (!contentText) {
                alert('글을 입력해주세요.');
                return;
            }
            try {
                uploadedItems.length = 0;
                const contentHTML = quill.root.innerHTML;
                const parser = new DOMParser();
                const doc = parser.parseFromString(contentHTML, 'text/html');
                const imgTags = doc.querySelectorAll('img');

                const endpoints = {
                    review: '/api/reviews',
                    contact: '/api/contacts',
                };

                for (const img of imgTags) {
                    const src = img.getAttribute('src');
                    if (src.startsWith('data:image')) {
                        const blob = await (await fetch(src)).blob();
                        const formData = new FormData();
                        const ext = blob.type.split('/')[1];
                        formData.append('file', blob, `image.${ext}`);

                        const res = await fetch('/api/files/upload', {
                            method: 'POST',
                            body: formData,
                        });

                        if (!res.ok) {
                            throw new Error('이미지 업로드 실패');
                        }

                        const {filePath} = await res.json();
                        uploadedFilePaths.push(filePath);
                        img.setAttribute('src', baseUrl + filePath);

                        uploadedItems.push({
                            filename: filePath.split('/').pop(),
                            filePath: filePath
                        });
                    } else {
                        let filePath = src;
                        if (src.startsWith(baseUrl)) {
                            filePath = src.replace(baseUrl, '');
                        }
                        uploadedItems.push({
                            filename: filePath.split('/').pop(),
                            filePath: filePath
                        });
                    }
                }

                const fileLinks = doc.querySelectorAll(
                    'a[href^="' + baseUrl + '"]');
                for (const aTag of fileLinks) {
                    const href = aTag.getAttribute('href');
                    let filePath = href.replace(baseUrl, '');
                    uploadedItems.push({
                        filename: filePath.split('/').pop(),
                        filePath: filePath
                    });
                }

                const cleanedHTML = doc.body.innerHTML;
                const endpoint = isEditMode ? `${endpoints[type]}/${itemId}`
                    : endpoints[type];
                const method = isEditMode ? 'PUT' : 'POST';

                const payload = {
                    title: document.getElementById('title').value,
                    content: cleanedHTML,
                    images: uploadedItems,
                };

                const response = await fetch(endpoint, {
                    method,
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(payload),
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || '저장 실패');
                }

                alert(isEditMode ? '수정이 완료되었습니다!' : '등록이 완료되었습니다!');
                if (isEditMode) {
                    const deletedFilePaths = initialFilePaths.filter(
                        path => !uploadedItems.some(
                            item => item.filePath === path));
                    for (const path of deletedFilePaths) {
                        try {
                            await fetch('/api/files/delete', {
                                method: 'POST',
                                headers: {'Content-Type': 'application/json'},
                                body: JSON.stringify({filePath: path}),
                            });
                        } catch (err) {
                            console.warn('이미지 삭제 실패:', path);
                        }
                    }
                }
                window.location.href = isEditMode ? `/${type}s/${itemId}`
                    : `/${type}s`;
            } catch (error) {
                console.error('저장 중 오류:', error);
                console.log('업로드된 파일들:', uploadedFilePaths);
                if (uploadedFilePaths.length > 0) {
                    for (const filePath of uploadedFilePaths) {
                        try {
                            await fetch('/api/files/delete', {
                                method: 'POST',
                                headers: {'Content-Type': 'application/json'},
                                body: JSON.stringify({filePath}),
                            });
                        } catch (deleteErr) {
                            console.warn('삭제 실패:', filePath);
                        }
                    }
                }

                alert(error.message || '저장 중 오류 발생');
            }
        });
    }

    const deleteBtn = document.getElementById('deleteBtn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', async () => {
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
                    const errorData = await response.json();
                    throw new Error(errorData.message || '삭제 실패');
                }

                alert('삭제가 완료되었습니다.');
                window.location.href = `/${type}s`;
            } catch (err) {
                console.error('삭제 중 오류:', err);
                alert(err.message || '삭제 중 오류 발생');
            }
        });
    }
});