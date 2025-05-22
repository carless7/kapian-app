const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

admin.initializeApp();

exports.notifyOnImageUpload = functions.storage.object().onFinalize(async (object) => {
  const filePath = object.name;

  if (!filePath.startsWith("images/")) {
    console.log("Not an image upload path. Skipping notification.");
    return null;
  }

  const pathParts = filePath.split("/");
  const userId = pathParts[1]; 

  if (!userId) {
    console.error("User ID not found in file path");
    return null;
  }

  try {
    const userDoc = await admin.firestore().collection("users").doc(userId).get();

    if (!userDoc.exists) {
      console.log(`User document for ${userId} does not exist.`);
      return null;
    }

    const fcmToken = userDoc.data().fcmToken;

    if (!fcmToken) {
      console.log(`No FCM token found for user ${userId}`);
      return null;
    }

    const message = {
      token: fcmToken,
      notification: {
        title: "Upload Complete",
        body: "Your buissnes card has been successfully uploaded.",
      },
    };

    const response = await admin.messaging().send(message);
    console.log(`Notification sent to ${userId}:`, response);

    return null;
  } catch (error) {
    console.error("Error sending notification:", error);
    return null;
  }
});
