/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {onRequest} = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");

const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.notifyOnImageUpload = functions.storage.object().onFinalize(async (object) => {
  const filePath = object.name;

  if (!filePath.startsWith("images/")) {
    console.log("Not an image upload path. Skipping notification.");
    return null;
  }

  const pathParts = filePath.split("/");
  const userId = pathParts[1]; // assuming path is like "images/{userId}/{filename}"

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
        body: "Your image has been successfully uploaded and processed.",
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
