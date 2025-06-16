let manVoice = null;
let womanVoice = null;

function loadVoicesAndSet() {
    const voices = speechSynthesis.getVoices();

    if (!voices.length) {
        // Retry loading voices after a short delay
        setTimeout(loadVoicesAndSet, 200);
        return;
    }

    // Log voices once for debugging
    console.log("Available voices:");
    voices.forEach(v => console.log(`${v.name} (${v.lang})`));

    // Get man and woman voices (adjust to match what your browser lists)
    manVoice = voices.find(v =>
        /David|Alex|Google UK English Male|Microsoft George|Fred/i.test(v.name)
    );

    womanVoice = voices.find(v =>
        /Zira|Samantha|Victoria|Google US English Female|Google UK English Female/i.test(v.name)
    );

    if (!manVoice) console.warn("No man voice found.");
    if (!womanVoice) console.warn("No woman voice found.");
}

// Ensure voices are loaded
if (typeof speechSynthesis !== "undefined") {
    if (speechSynthesis.getVoices().length > 0) {
        loadVoicesAndSet();
    } else {
        speechSynthesis.onvoiceschanged = loadVoicesAndSet;
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const categorySelect = document.getElementById('category-hidden');

    const inputText = document.getElementById('input-text');
    const inputResultGuide = document.getElementById('input-result-guide');

    const grammarTypeSelect = document.getElementById('grammarType-select');

    const readingPartSelect = document.getElementById('readingPart-select');
    const listeningPartSelect = document.getElementById('listeningPart-select');

    const searchBtn = document.getElementById("search-btn");
    const startBtn = document.getElementById("start-btn");

    const resultBox = document.getElementById("result-box");
    const resultText = document.getElementById("result-text");

    const playQuestionBtn = document.getElementById("play-question-btn");
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


    // *** allen part ***
    updatePlaceholder();

    let hasCalledAllen = false;

    // When select category
    window.selectCategory = function(btn) {
        //1. Activate selected category button
        const category = btn.getAttribute("data-category");
        document.getElementById("category-hidden").value = category;

        document.querySelectorAll('[data-category]').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        //2. Display or hide items based on selected category
        resultBox.style.display = "none";

        if (["문법","문장 체크", "어휘 목록", "어휘 설명"].includes(category)) {
            inputResultGuide.style.display = "inline";

            if (category === "문법") {
                inputText.style.display = "none";
                grammarTypeSelect.style.display = "inline";
            } else {
                inputText.style.display = "inline";
                grammarTypeSelect.style.display = "none";
            }

            inputText.value = "";
            readingPartSelect.style.display = "none";
            listeningPartSelect.style.display = "none";
            searchBtn.style.display = "inline";
            startBtn.style.display = "none";

            updatePlaceholder(category);    //update placeholder based on selected category (for non quiz type only)
        } else {
            inputResultGuide.style.display = "none";
            inputText.style.display = "none";
            searchBtn.style.display = "none";
            grammarTypeSelect.style.display = "none";

            if (category === "읽기 퀴즈") {
                readingPartSelect.disabled = false;
                readingPartSelect.style.display = "inline";
                listeningPartSelect.style.display = "none";
            } else {
                listeningPartSelect.disabled = false;
                listeningPartSelect.style.display = "inline";
                readingPartSelect.style.display = "none";
            }
            startBtn.disabled = false;
            startBtn.style.display = "inline";
        }

        //3. Reset Allen state
        if (hasCalledAllen) {
            resetAllenState();
            hasCalledAllen = false;
        }

    }

    function updatePlaceholder(category) {
        const placeholders = {
            "문장 체크": "문장을 입력하세요...",
            "어휘 목록": "주제를 입력하세요...",
            "어휘 설명": "단어를 입력하세요..."
        };

        inputText.placeholder = placeholders[category] || "Start typing...";
    }

    function resetAllenState() {
        fetch(`/api/resetAllen`, {
            method: 'DELETE'
        }).catch((err) => {
            console.warn('Failed to reset conversation:', err);
        });
    }

    //Search button for non-quiz category
    if (searchBtn) {
        searchBtn.addEventListener("click", (e) => {
            searchBtn.disabled = true;

            e.preventDefault();
            let input = "";

            if (categorySelect.value === "문법") {
                input = grammarTypeSelect.value;
            } else {
                input = inputText.value.trim();
            }

            // Input validation before call allen api
            if (categorySelect.value !== "문법" && !validateEnglishInput(input)) {
                inputText.focus();
                return;
            }

            callAllenAPI(input);
        });
    }

    //Validate input must be in english alphabet
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

    //Quiz start button (reading and listening)
    if (startBtn) {
        startBtn.addEventListener("click", (e) => {
            let inputVal = "";

            if (categorySelect.value === "읽기 퀴즈") {
                readingPartSelect.disabled = true;
                inputVal = readingPartSelect.value;
            } else {
                listeningPartSelect.disabled = true;
                inputVal = listeningPartSelect.value;
            }
            startBtn.disabled = true;

            nextQuestionBtn.disabled = false;
            stopQuestionBtn.disabled = false;

            document.querySelectorAll('[data-category]').forEach(btn => {
                btn.classList.add("disabled-category");
                btn.setAttribute("data-disabled", "true");
            });

            callAllenAPI(inputVal);
        });
    }

    //Quiz next question button (reading and listening)
    if (nextQuestionBtn) {
        nextQuestionBtn.addEventListener("click", (e) => {
            speechSynthesis.cancel();

            let inputVal = "";
            if (categorySelect.value === "읽기 퀴즈") {
                inputVal = readingPartSelect.value;
            } else {
                inputVal = listeningPartSelect.value;
            }

            callAllenAPI(inputVal);
        });
    }

    //Quiz stop quiz button (reading and listening)
    if (stopQuestionBtn) {
        stopQuestionBtn.addEventListener("click", (e) => {
            beforeQuizMode ();
        });
    }

    function beforeQuizMode () {
        if (categorySelect.value === "읽기 퀴즈") {
            readingPartSelect.disabled = false;
        } else {
            speechSynthesis.cancel();
            isSpeechCancelled = true;

            playQuestionBtn.disabled = true;
            listeningPartSelect.disabled = false;
        }

        startBtn.disabled = false;
        resultBox.style.display = "none";

        nextQuestionBtn.disabled = true;
        stopQuestionBtn.disabled = true;

        document.querySelectorAll('[data-category]').forEach(btn => {
            btn.classList.remove("disabled-category");
            btn.removeAttribute("data-disabled");
        });
    }

    // Call allen api with input
    function callAllenAPI(input) {
        let category = categorySelect.value;
        resultText.innerHTML = "";

        // Show spinner and hide previous answer
        loadingSpinner.style.display = "block";
        resultBox.style.display = "none";

        hasCalledAllen = true;

        fetch("/api/askAllen", {
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
                    console.log("Response incomplete: " + errorData.errorMessage);
                    beforeQuizMode();
                } else {
                    return response.json();
                }
            })
            .then(data => {
                if (category === "읽기 퀴즈" || category === "듣기 퀴즈") {
                    renderQuizData(data.allenInputText, data.quizData);
                    resultBox.style.display = "block";
                }else {
                    resultText.innerHTML = data.allenContent;
                    resultBox.style.display = "block";
                    return saveAllenInfo(category, data.allenInputText, data.allenContent);
                }
            })
            .catch(error => {
                console.error("Fetch error:", error);
                beforeQuizMode();
            })
            .finally(() => {
                // Hide spinner after fetch completes
                loadingSpinner.style.display = "none";
                searchBtn.disabled = false;
            });

    }

    let currentQuizData = null;
    let isSpeechCancelled = false;

    //Render reading and listening quiz data
    function renderQuizData(allenInputText, quizData) {
        currentQuizData = quizData; // Save for playback

        const { passage, question, answerChoices, correctAnswer } = quizData;

        const passageHTML = `<p><strong>Passage:</strong><br>${marked.parse(passage)}</p>`;
        const questionHTML = `<p><strong>Question:</strong><br>${question}</p>`;
        const optionsHTML = Object.entries(answerChoices).map(([key, value]) =>
        `<label for="choice-${key}" style="display: inline-flex; align-items: center; margin-bottom: 8px; cursor: pointer;">
        <input type="radio" name="answer" value="${key}" id="choice-${key}" style="margin-right: 8px; cursor: pointer;">${key}) ${value}</label><br>`).join('');
        const feedbackHTML = `<div id="quiz-feedback" class="mt-2 text-center"></div>`;

        if (categorySelect.value === "읽기 퀴즈") {
            resultText.innerHTML = passageHTML + questionHTML + `<div id="quiz-options">${optionsHTML}</div>` + feedbackHTML;
            playQuestionBtn.disabled = true;
            playQuestionBtn.style.display = "none";
        } else {
            resultText.innerHTML = `<div id="quiz-options">${optionsHTML}</div>` + feedbackHTML;
            playQuestionBtn.disabled = false;
            playQuestionBtn.style.display = "inline-block";
        }

        // Hide the "Next Question" button initially
        nextQuestionBtn.style.display = "none";
        stopQuestionBtn.style.display = "inline-block";

        // Attach event listener to each radio input
        document.querySelectorAll('input[name="answer"]').forEach(input => {
            input.addEventListener("change", function () {
                const selected = this.value;
                const feedback = document.getElementById("quiz-feedback");

                // Clear previous feedback
                feedback.innerHTML = "";

                // Create a container for animation
                const animationContainer = document.createElement("div");
                animationContainer.style.width = "80px";
                animationContainer.style.height = "80px";
                animationContainer.style.margin = "0 auto";
                feedback.appendChild(animationContainer);

                // Determine correct animation path
                const animationPath = selected === correctAnswer
                    ? "animations/success.json"
                    : "animations/fail.json";

                // Load animation using Lottie
                lottie.loadAnimation({
                    container: animationContainer,
                    renderer: "svg",
                    loop: false,
                    autoplay: true,
                    path: animationPath
                });

                const feedbackText = document.createElement("p");
                feedbackText.innerHTML = selected === correctAnswer
                    ? `<strong class="text-success">Correct!</strong>`
                    : `<strong class="text-danger">Incorrect.</strong> Correct answer is ${correctAnswer}.`;
                feedback.appendChild(feedbackText);

                // Disable all options after selection
                document.querySelectorAll('input[name="answer"]').forEach(input => input.disabled = true);

                // Show the "Next Question" button after answer
                nextQuestionBtn.style.display = "inline-block";
                isSpeechCancelled = true;
                speechSynthesis.cancel();

                const summary = `Question: ${question}\nSelected Answer: ${selected}\nCorrect Answer: ${correctAnswer}`;
                saveAllenInfo(categorySelect.value, allenInputText, summary)
                    .then(response => {
                        if (!response.ok) {
                            console.warn("Failed to save answer");
                        }
                    })
                    .catch(err => {
                        console.error("Error saving answer:", err);
                    });
            });
        });
    }

    // Play question (only for listening quiz)
    if (playQuestionBtn) {
        playQuestionBtn.addEventListener("click", (e) => {
            speechSynthesis.cancel(); // stop any ongoing speech
            isSpeechCancelled = false;
            playQuestionBtn.disabled = true; //disable button when reading

            const rawPassage = currentQuizData?.passage || "";
            const withoutTags = rawPassage.replace(/<[^>]+>/g, '').trim();  //remove tags

            const parser = new DOMParser();
            const passageText = parser.parseFromString(withoutTags, 'text/html').documentElement.textContent; // Decode HTML entities like &quot;, &amp;, etc.

            const questionText = currentQuizData?.question || "";

            const lines = [];

            if (passageText) {
                lines.push(...passageText.split('\n').filter(line => line.trim()));
            }

            if (questionText) {
                lines.push("Question: " + questionText);
            }

            let index = 0;

            function speakNext() {
                if (isSpeechCancelled || index >= lines.length) {
                    playQuestionBtn.disabled = false; // Enable button after reading last line
                    return;
                }

                const rawLine = String(lines[index] || "").trim();
                index++;

                if (!rawLine) {
                    speakNext();
                    return;
                }

                const utterance = new SpeechSynthesisUtterance();
                utterance.lang = "en-US";
                utterance.rate = 0.95;

                if (rawLine.startsWith("Man:")) {
                    utterance.voice = manVoice;
                    utterance.text = rawLine.replace("Man:", "").trim();
                } else if (rawLine.startsWith("Woman:")) {
                    utterance.voice = womanVoice;
                    utterance.text = rawLine.replace("Woman:", "").trim();
                } else {
                    utterance.voice = womanVoice; // default voice
                    utterance.text = rawLine;
                }

                // Skip next if cancel was triggered mid-speech
                utterance.onend = () => {
                    if (!isSpeechCancelled) {
                        speakNext();
                    }
                };

                speechSynthesis.speak(utterance);
            }

            speakNext();
        });
    }

    function saveAllenInfo(category, inputText, summary) {
        let userId = document.getElementById("user-id");

        return fetch("/api/allen", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                userId: userId.value,
                category: category,
                inputText: inputText,
                summary: summary
            })
        });
    }

    // *** TIL part ***
    // Display note writing
    tilDefaultMode();

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
            tilDefaultMode();
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

            tilDefaultMode();
        })
    }

    //Default state of writing note
    function tilDefaultMode() {
        writeNoteButton.style.display = "inline";
        saveNoteButton.style.display = "none";
        cancelNoteButton.style.display = "none";

        tilTitleGroup.style.display = "none";
        tilContentGroup.style.display = "none";
    }
})