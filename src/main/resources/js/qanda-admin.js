//
// Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
// or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
//

var QANDAADMIN = (function() {

    function addExpertGroup(baseUrl) {
        AJS.$.ajax({
            type: 'POST',
            url: baseUrl + "/rest/agrade/qanda/latest/qandaconfig/addExpertGroup",
            data: {
                "projectKey" : AJS.$('#projectKey').val(),
                "name" : AJS.$('#addGroupName').val(),
                "description" : AJS.$('#addGroupDescr').val(),
                "members" : AJS.$('#addGroupMembers').val()
            },
            success: function(data){
                if(!data.error) {

                } else {
                    //::TODO:: print wrong users, duplicate name, empty fields
                }
                reloadWindow();
            }
        });
    }

    function reloadWindow() {
        AJS.dim();
        window.location.reload();
    }

    function initDialogTrigger(context){
        context.find("a.qanda-trigger-dialog").each(function () {
            new JIRA.FormDialog({
                trigger: this,
                id: this.id + "-dialog",
                ajaxOptions: {
                    url: this.href,
                    data: {
                        decorator: "dialog",
                        inline: "true",
                        returnUrl : window.location
                    }
                }
            });
        });
    }

    JIRA.bind(JIRA.Events.ISSUE_REFRESHED, function (e, context, reason) {
        initDialogTrigger(AJS.$(document));
    });

    AJS.$(document).ready(function(){
        initDialogTrigger(AJS.$(this));
        AJS.$(".qanda-confirm-delete").bind("click", function(){
            if(!confirm("Are you sure you want to delete this Expert Group?")){
                return false;
            }
        })
    });

    return {
        addExpertGroup: addExpertGroup
    };
})();