document.addEventListener("DOMContentLoaded",function (){
    document.getElementById("lodging-update").addEventListener("click", function (){
        window.location.href = "../../../../templates/views/mypage/manager/ApprovalLodgingUpdate.html"
    });
    document.getElementById("member-management").addEventListener("click", function (){
        window.location.href = "../../../../templates/views/mypage/manager/MemberManagement.html"
    });
    document.getElementById("manage-info-btn").addEventListener("click", function (){
        window.location.href = "../../../../templates/views/mypage/customer/ManageAccount.html"
    });
});

function addRoleProvider(userId) {
    fetch('/mypage/manager/addRoleProvider', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ userId: userId })
    }).then(response => {
        if (response.ok) {
            window.location.reload();
        } else {
            alert('Failed to add ROLE_PROVIDER');
        }
    });
}

function addRoleMaster(userId) {
    fetch('/mypage/manager/addRoleMaster', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ userId: userId })
    }).then(response => {
        if (response.ok) {
            window.location.reload();
        } else {
            alert('Failed to add ROLE_MASTER');
        }
    });
}