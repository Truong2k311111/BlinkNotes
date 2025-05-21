const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendNotification = functions.https.onCall(async (data, context) => {
    const { token, title, body, senderId, senderName } = data;

    const payload = {
        notification: {
            title: title,
            body: body,
        },
        data: {
            click_action: "OPEN_CHAT",
            sender_id: senderId,
            sender_name: senderName,
        },
        token: token,
    };

    try {
        const response = await admin.messaging().send(payload);
        return { success: true, response };
    } catch (error) {
        console.error("Error sending FCM", error);
        throw new functions.https.HttpsError("unknown", "Failed to send FCM");
    }
});
