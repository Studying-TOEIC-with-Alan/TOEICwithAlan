const uploadedImages = [];
// Quill 에디터 초기화
const quill = new Quill('#editor', {
    theme: 'snow',
    placeholder: '내용을 작성하세요...',
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

            const range = quill.getSelection();
            quill.insertEmbed(range.index, 'image', imageUrl);
        } catch (err) {
            console.error('이미지 업로드 실패:', err);
        }
    };
}

// 저장 버튼 클릭
document.getElementById('submitBtn').addEventListener('click', async () => {
    const type = document.body.dataset.type;
    const endpoint = type === 'contact' ? '/api/contacts' : '/api/reviews';

    const title = document.getElementById('title').value;
    const content = quill.root.innerHTML;

    const reviewObj = {title, content, images: uploadedImages};

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(reviewObj)
        });

        if (!response.ok) {
            const errText = await response.text();
            throw new Error(`HTTP ${response.status} - ${errText}`);
        }

        const result = await response.json();
        console.log('업로드 성공:', result);
        alert('등록이 완료되었습니다!');
    } catch (error) {
        console.error('업로드 실패:', error.message);
        alert('등록에 실패했습니다. 다시 시도해주세요.');
    }
});

