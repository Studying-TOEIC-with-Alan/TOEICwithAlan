document.addEventListener("DOMContentLoaded", function () {
    const writeNoteButton = document.getElementById("write-note-button");
    const saveNoteButton = document.getElementById("save-note-button");
    const cancelNoteButton = document.getElementById("cancel-note-button");

    const tilTitleGroup = document.getElementById("tilTitleGroup");
    const tilContentGroup = document.getElementById("tilContentGroup");

    const tilTitle = document.getElementById("tilTitle");
    const tilContent = document.getElementById("tilContent");

    defaultMode();

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
})