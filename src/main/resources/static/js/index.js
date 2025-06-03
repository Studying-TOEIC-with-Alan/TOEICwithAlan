document.addEventListener("DOMContentLoaded", function () {
    const categorySelect = document.getElementById('category-hidden');
    const inputText = document.getElementById('input-text');
    const partNameSelect = document.getElementById('partName-select');
    const inputResultGuide = document.getElementById('input-result-guide');

    const searchBtn = document.getElementById("search-btn");
    const startBtn = document.getElementById("start-btn");

    const resultBox = document.getElementById("result-box");
    const resultText = document.getElementById("result-text");

    const nextQuestionBtn = document.getElementById("next-question-btn");
    const stopQuestionBtn = document.getElementById("stop-question-btn");

    const loadingSpinner = document.getElementById("loading-spinner");

    const writeNoteButton = document.getElementById("write-note-button");
    const saveNoteButton = document.getElementById("save-note-button");
    const cancelNoteButton = document.getElementById("cancel-note-button");

    const tilTitleGroup = document.getElementById("tilTitleGroup");
    const tilContentGroup = document.getElementById("tilContentGroup");

    const tilTitle = document.getElementById("tilTitle");
    const tilContent = document.getElementById("tilContent");

    updatePlaceholder();
    defaultMode();

    let hasCalledAllen = false;

    window.selectCategory = function(btn) {
        const category = btn.getAttribute("data-category");
        document.getElementById("category-hidden").value = category;

        document.querySelectorAll('[data-category]').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        resultText.innerHTML = "";

        if (["문법","문장 체크", "어휘 목록", "어휘 설명"].includes(category)) {
            inputResultGuide.style.display = "inline";
            if (category === "문법") {
                inputText.style.display = "none";
            } else {
                inputText.style.display = "inline";
            }
            inputText.value = "";
            partNameSelect.style.display = "none";
            searchBtn.style.display = "inline";
            startBtn.style.display = "none";
        } else {
            inputResultGuide.style.display = "none";
            inputText.style.display = "none";
            searchBtn.style.display = "none";

            partNameSelect.disabled = false;
            startBtn.disabled = false;
            partNameSelect.style.display = "inline";
            startBtn.style.display = "inline";
        }

        //Reset Allen state
        if (hasCalledAllen) {
            resetAllenState();
            hasCalledAllen = false;
        }

        updatePlaceholder(category);
    }

    function resetAllenState() {
        fetch(`/api/allen`, {
            method: 'DELETE'
        }).catch((err) => {
            console.warn('Failed to reset conversation:', err);
        });
    }

    //Update placeholder when user select category
    function updatePlaceholder(category) {
        const placeholders = {
            "문장 체크": "문장을 입력하세요...",
            "어휘 목록": "주제를 입력하세요...",
            "어휘 설명": "단어를 입력하세요..."
        };

        inputText.placeholder = placeholders[category] || "Start typing...";
    }

    if (searchBtn) {
        searchBtn.addEventListener("click", (e) => {
            e.preventDefault();
            const input = inputText.value.trim();

            // Validation before call allen api
            if (categorySelect.value !== "문법" && !validateEnglishInput(input)) {
                inputText.focus();
                return;
            }

            callAllenAPI(input);
        });
    }

    //Validate input to allen must be in english alphabet
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

    if (startBtn) {
        startBtn.addEventListener("click", (e) => {
            partNameSelect.disabled = true;
            startBtn.disabled = true;

            nextQuestionBtn.disabled = false;
            stopQuestionBtn.disabled = false;

            callAllenAPI(partNameSelect.value);
        });
    }

    // Submit to call allen api
    function callAllenAPI(input) {
        let category = categorySelect.value;
        resultText.innerHTML = "";

        // Show spinner and hide previous answer
        loadingSpinner.style.display = "block";
        resultBox.style.display = "none";

        hasCalledAllen = true;

        fetch("/api/allen", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            body: new URLSearchParams({
                category: category,
                inputText: input
            })
        })
            .then(async  response => {
                if (!response.ok) {
                    const errorData = await response.json();
                    alert(errorData.errorMessage);
                }
                return category === "일기 퀴즈" ? response.json() : response.text();
            })
            .then(data => {
                if (category === "일기 퀴즈") {
                    renderQuizData(data);
                } else {
                    resultText.innerHTML = data;
                }
                resultBox.style.display = "block";
            })
            .catch(error => {
                resultText.innerHTML = "<p class='text-danger'>${error.message}</p>";
                resultBox.style.display = "block";
            })
            .finally(() => {
                // Hide spinner after fetch completes
                loadingSpinner.style.display = "none";
            });

    }

    //Render quiz data
    function renderQuizData(data) {
        const { passage, question, answerChoices, correctAnswer } = data;

        const passageHTML = `<p><strong>Passage:</strong><br>${passage}</p>`;
        const questionHTML = `<p><strong>Question:</strong><br>${question}</p>`;
        const optionsHTML = Object.entries(answerChoices).map(([key, value]) =>
        `<label for="choice-${key}" style="display: inline-flex; align-items: center; margin-bottom: 8px; cursor: pointer;">
        <input type="radio" name="answer" value="${key}" id="choice-${key}" style="margin-right: 8px; cursor: pointer;">${key}) ${value}</label><br>`).join('');
        const feedbackHTML = `<div id="quiz-feedback" class="mt-2"></div>`;

        resultText.innerHTML = passageHTML + questionHTML + `<div id="quiz-options">${optionsHTML}</div>` + feedbackHTML;

        // Hide the "Next Question" and "Stop Question" button initially
        nextQuestionBtn.style.display = "none";
        stopQuestionBtn.style.display = "none";

        // Attach event listener to each radio input
        document.querySelectorAll('input[name="answer"]').forEach(input => {
            input.addEventListener("change", function () {
                const selected = this.value;
                const feedback = document.getElementById("quiz-feedback");

                if (selected === correctAnswer) {
                    feedback.innerHTML = `<p class="text-success"><strong>Correct!</strong></p>`;
                } else {
                    feedback.innerHTML = `<p class="text-danger"><strong>Incorrect.</strong> Correct answer is ${correctAnswer}.</p>`;
                }

                // Optional: disable all options after selection
                document.querySelectorAll('input[name="answer"]').forEach(input => input.disabled = true);

                // Show the "Next Question" and "Stop Question" button after answer
                nextQuestionBtn.style.display = "inline-block";
                stopQuestionBtn.style.display = "inline-block";
            });
        });
    }

    if (nextQuestionBtn) {
        nextQuestionBtn.addEventListener("click", (e) => {
            callAllenAPI(partNameSelect.value);
        });
    }

    if (stopQuestionBtn) {
        stopQuestionBtn.addEventListener("click", (e) => {
            partNameSelect.disabled = false;
            startBtn.disabled = false;

            nextQuestionBtn.disabled = true;
            stopQuestionBtn.disabled = true;
        });
    }

    // Display note writing
    if (writeNoteButton) {
        writeNoteButton.addEventListener('click', e => {
            writeNoteButton.style.display = "none";
            saveNoteButton.style.display = "inline";
            cancelNoteButton.style.display = "inline";

            tilTitleGroup.style.display = "block";
            tilContentGroup.style.display = "block";
            tilTitle.value = "";
            tilContent.value = "";
        })
    }

    // Cancel note writing
    if (cancelNoteButton) {
        cancelNoteButton.addEventListener('click', e => {
            defaultMode();
        })
    }

    // Save note
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

            let userId = document.getElementById("user-id");

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

    //Default state of writing note
    function defaultMode() {
        writeNoteButton.style.display = "inline";
        saveNoteButton.style.display = "none";
        cancelNoteButton.style.display = "none";

        tilTitleGroup.style.display = "none";
        tilContentGroup.style.display = "none";
    }
})
