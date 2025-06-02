document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("ask-allen-form");
    const categorySelect = document.getElementById('category-select');
    const inputText = document.getElementById('input-text');

    const writeNoteButton = document.getElementById("write-note-button");
    const saveNoteButton = document.getElementById("save-note-button");
    const cancelNoteButton = document.getElementById("cancel-note-button");

    const tilTitleGroup = document.getElementById("tilTitleGroup");
    const tilContentGroup = document.getElementById("tilContentGroup");

    const tilTitle = document.getElementById("tilTitle");
    const tilContent = document.getElementById("tilContent");

    updatePlaceholder();
    defaultMode();

    if (categorySelect) {
        categorySelect.addEventListener('change', updatePlaceholder);
    }

    //Validation before input to allen api - start
    form.addEventListener("submit", function (e) {
        const value = inputText.value.trim();

        if (!validateEnglishInput(value)) {
            e.preventDefault();     //prevent submitting
            inputText.focus();
        }
    });

    function validateEnglishInput(value) {
        if (value === "") {
            alert("입력란이 비어 있습니다. 내용을 입력해주세요");
            return false;
        }
        if (!/^[A-Za-z0-9\s.,!?'"()\-]*$/.test(value)) {
            alert("영어 알파벳만 입력해주세요");
            return false;
        }
        return true;
    }
    //Validation before input to allen api - end

    if (writeNoteButton) {
        writeNoteButton.addEventListener('click', e => {
            writeNoteButton.style.display = "none";
            saveNoteButton.style.display = "inline";
            cancelNoteButton.style.display = "inline";

            tilTitleGroup.style.display = "block";
            tilContentGroup.style.display = "block";
        })
    }

    if (cancelNoteButton) {
        cancelNoteButton.addEventListener('click', e => {
            defaultMode();
        })
    }

    if (saveNoteButton) {
        saveNoteButton.addEventListener('click', e => {
            if (tilTitle.value.trim() === "") {
                alert("제목 입력해주세요");
                tilTitle.focus();
                return;
            }

            if (tilContent.value.trim() === "") {
                alert("내용 입력해주세요");
                tilContent.focus();
                return;
            }

            userId = document.getElementById("user-id");

            fetch(`/api/til`, {
                method: 'POST',
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    userId: userId.value,
                    title: tilTitle.value,
                    summary: tilContent.value
                })
            }).then(() => {
                alert("저장 되었습니다");
                tilTitle.value = "";
                tilContent.value = "";
            })

            defaultMode();
        })
    }

    function defaultMode() {
        writeNoteButton.style.display = "inline";
        saveNoteButton.style.display = "none";
        cancelNoteButton.style.display = "none";

        tilTitleGroup.style.display = "none";
        tilContentGroup.style.display = "none";
    }

    function updatePlaceholder() {
        const category = categorySelect.value;

        const placeholders = {
            "문장 체크": "문장을 입력하세요...",
            "어휘 목록": "주제를 입력하세요...",
            "어휘 설명": "단어를 입력하세요..."
        };

        inputText.placeholder = placeholders[category] || "Start typing...";
    }
})
