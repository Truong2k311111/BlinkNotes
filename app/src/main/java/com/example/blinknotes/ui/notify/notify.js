// functions/index.js

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendMessageNotification = functions.firestore
    .document('contentchat/{messageId}')
    .onCreate(async (snap, context) => {
        const message = snap.data();

        const receiverId = message.receiverId;
        const senderId = message.senderId;
        const content = message.content;

        // Get receiver's FCM token
        const userDoc = await admin.firestore().collection('users').doc(receiverId).get();
        const fcmToken = userDoc.get('fcmToken');
        const senderDoc = await admin.firestore().collection('users').doc(senderId).get();
        const senderName = senderDoc.get('username') || 'Bạn bè';

        if (!fcmToken) return;

        const payload = {
            notification: {
                title: `Tin nhắn từ ${senderName}`,
                body: content,
                clickAction: "FLUTTER_NOTIFICATION_CLICK", // nếu dùng Flutter hoặc tùy platform
            }
        };

        return admin.messaging().sendToDevice(fcmToken, payload);
    });
