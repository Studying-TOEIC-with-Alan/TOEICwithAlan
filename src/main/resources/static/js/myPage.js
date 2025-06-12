document.addEventListener("DOMContentLoaded", function () {
    const nicknameDefaultGroup = document.getElementById('nickname-default-group');
    const nicknameEditGroup = document.getElementById('nickname-edit-group');

    const nicknameEditBtn = document.getElementById('nickname-edit-btn');
    const nicknameSaveBtn = document.getElementById('nickname-save-btn');
    const nicknameCancelBtn = document.getElementById('nickname-cancel-btn');

    let nicknameText = document.getElementById('profile-nickname-text');
    let nicknameInput = document.getElementById('profile-nickname-input');

    const terminateAccountBtn = document.getElementById('terminate-account-button');

    defaultMode();

    if (nicknameEditBtn) {
        nicknameEditBtn.addEventListener('click', e => {
            nicknameDefaultGroup.style.display = "none";
            nicknameEditGroup.style.display = "block";
        })
    }

    if (nicknameSaveBtn) {
        nicknameSaveBtn.addEventListener('click', e => {
            let oriNickname = nicknameText.textContent.trim();
            let newNickname = nicknameInput.value.trim();

            if (newNickname === "") {
                alert("닉네임을 입력하세요");
            } else if (oriNickname === newNickname) {
                alert("닉네임을 안 바꿨습니다");
                defaultMode();
            } else {
                userId = document.getElementById("user-id");

                fetch(`/api/user/${userId.value}`, {
                    method: 'PUT',
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        nickname: newNickname
                    })
                }).then(async (response) => {
                    if (!response.ok) {
                        const errorData = await response.json();
                        alert (errorData.errorMessage);
                    } else {
                        alert("수정 되었습니다");
                        window.location.href = `/myPage`;
                    }
                })
            }
        })
    }

    if (nicknameCancelBtn) {
        nicknameCancelBtn.addEventListener('click', e => {
            defaultMode();
        })
    }

    if (terminateAccountBtn) {
        terminateAccountBtn.addEventListener('click', e => {
            const confirmed = confirm("탈퇴하시겠습니까?");
            if (confirmed) {
                userId = document.getElementById("user-id");

                fetch(`/api/user/${userId.value}`, {
                    method: 'PUT',
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        isActive: "N",
                        terminationDate: new Date().toISOString().split("T")[0]
                    })
                }).then(async (response) => {
                    if (!response.ok) {
                        const errorData = await response.json();
                        alert(errorData.errorMessage);
                    } else {
                        alert("탈퇴 되었습니다");
                        window.location.href = `/logout`;
                    }
                })
            }
        })
    }

    function defaultMode() {
        nicknameDefaultGroup.style.display = "block";
        nicknameEditGroup.style.display = "none";

        //Return back nickname value when user cancel to change
        if (nicknameInput.value !== nicknameText.textContent) {
            nicknameInput.value = nicknameText.textContent;
        }
    }
})