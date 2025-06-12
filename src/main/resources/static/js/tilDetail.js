document.addEventListener("DOMContentLoaded", function () {
    const editNoteButton = document.getElementById("edit-note-button");
    const deleteNoteButton = document.getElementById("delete-note-button");
    const saveNoteButton = document.getElementById("save-note-button");
    const cancelNoteButton = document.getElementById("cancel-note-button");

    const tilHeader = document.getElementById("til-header");

    const tilTitleText = document.getElementById("til-title-text");
    const tilTitleInput = document.getElementById("til-title-input");

    const tilContentText = document.getElementById("til-content-text");
    const tilContentInput = document.getElementById("til-content-input");

    const tilId = document.getElementById("til-id");
    const tilDate = document.getElementById("til-date");

    defaultMode();

    if (editNoteButton) {
        editNoteButton.addEventListener('click', e => {
            editNoteButton.style.display = "none";
            deleteNoteButton.style.display = "none";
            saveNoteButton.style.display = "inline";
            cancelNoteButton.style.display = "inline";

            tilTitleText.style.display = "none";
            tilTitleInput.style.display = "inline";

            tilContentText.style.display = "none";
            tilContentInput.style.display = "inline";

            tilDate.style.display = "none";
            tilHeader.classList.remove("border-bottom");
        })
    }

    if (deleteNoteButton) {
        deleteNoteButton.addEventListener('click', e => {
            const confirmed = confirm("삭제하시겠습니까?");
            if (confirmed) {
                fetch(`/api/til/${tilId.value}`, {
                    method: 'DELETE'
                }).then(() => {
                    alert("삭제 되었습니다");
                    window.location.href = '/til';
                })
            }
        })
    }

    if (saveNoteButton) {
        saveNoteButton.addEventListener('click', e => {
            fetch(`/api/til/${tilId.value}`, {
                method: 'PUT',
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    title: tilTitleInput.value,
                    summary: tilContentInput.value
                })
            }).then(() => {
                alert("수정 되었습니다");
                window.location.href = `/til/${tilId.value}`;
            })
        })
    }

    if (cancelNoteButton) {
        cancelNoteButton.addEventListener('click', e => {
            defaultMode();
        })
    }

    function defaultMode() {
        editNoteButton.style.display = "inline";
        deleteNoteButton.style.display = "inline";
        saveNoteButton.style.display = "none";
        cancelNoteButton.style.display = "none";

        tilTitleText.style.display = "inline";
        tilTitleInput.style.display = "none";

        tilContentText.style.display = "inline";
        tilContentInput.style.display = "none";

        tilDate.style.display = "block";
        tilHeader.classList.add("border-bottom");
    }

})