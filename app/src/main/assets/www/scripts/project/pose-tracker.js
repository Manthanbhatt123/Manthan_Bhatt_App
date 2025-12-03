// import { PoseLandmarker, FilesetResolver } from "https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision/vision_bundle.js";

// let poseLandmarker, video, lastTimestamp = 0;

// async function initPoseDetection() {
//   const vision = await FilesetResolver.forVisionTasks(
//     "https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision/wasm"
//   );

//   poseLandmarker = await PoseLandmarker.createFromOptions(vision, {
//     baseOptions: {
//       modelAssetPath:
//         "https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task",
//     },
//     runningMode: "VIDEO",
//     numPoses: 1,
//   });

//   video = document.createElement("video");
//   video.autoplay = true;
//   video.playsInline = true;
//   video.style.position = "fixed";
//   video.style.top = "0";
//   video.style.left = "0";
//   video.style.transform = "scaleX(-1)";
//   video.style.zIndex = "2";
//   video.style.opacity = "1";
//   video.width = 320;
//   video.height = 220;
//   document.body.appendChild(video);
  

//   const stream = await navigator.mediaDevices.getUserMedia({ video: true });
//   video.srcObject = stream;

//   video.onloadedmetadata = () => {
//     video.play();
//     detectLoop();
//   };
// }

// const DETECTION_INTERVAL = globalThis.runtime?.globalVars?.DetectionSpeed || 85;

// async function detectLoop() {
//   const now = performance.now();
//   if (now - lastTimestamp < DETECTION_INTERVAL) {
//     requestAnimationFrame(detectLoop);
//     return;
//   }
//   lastTimestamp = now;

//   if (!poseLandmarker || video.readyState !== 4) {
//     requestAnimationFrame(detectLoop);
//     return;
//   }

//   const result = await poseLandmarker.detectForVideo(video, now);

//   if (result.landmarks && result.landmarks.length > 0) {
//     const landmarks = result.landmarks[0];

//     const leftHand = landmarks[19];  
//     const rightHand = landmarks[20]; 

//     const canvas = document.querySelector("canvas");
//     const w = canvas.width;
//     const h = canvas.height;

//     globalThis.runtime.globalVars.LeftHandX = (1 - leftHand.x) * w;
//     globalThis.runtime.globalVars.LeftHandY = leftHand.y * h;
//     globalThis.runtime.globalVars.RightHandX = (1 - rightHand.x) * w;
//     globalThis.runtime.globalVars.RightHandY = rightHand.y * h;
    
//   }
//     requestAnimationFrame(detectLoop);
// }

// runOnStartup(async runtime => {
//   globalThis.runtime = runtime;
//   await initPoseDetection();
// });


// import { PoseLandmarker, FilesetResolver } from "https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision/vision_bundle.js";
// let video; 
// let poseLandmarker, lastTimestamp = 0;

// // Dynamically create the camera prompt UI
// function createCameraPromptUI() {
//   const prompt = document.createElement("div");
//   prompt.id = "cameraPrompt";
//   prompt.style.position = "fixed";
//   prompt.style.zIndex = "1000";
//   prompt.style.top = "0";
//   prompt.style.left = "0";
//   prompt.style.width = "100vw";
//   prompt.style.height = "100vh";
//   prompt.style.background = "rgba(0,0,0,0.8)";
//   prompt.style.display = "flex";
//   prompt.style.flexDirection = "column";
//   prompt.style.justifyContent = "center";
//   prompt.style.alignItems = "center";

//   prompt.innerHTML = `
//     <p style="color: white; font-size: 18px;">Do you want to enable the camera?</p>
//     <button id="yesButton" style="margin: 10px; padding: 10px 20px;">Yes</button>
//     <button id="noButton" style="margin: 10px; padding: 10px 20px;">No</button>
//   `;

//   document.body.appendChild(prompt);

// document.getElementById("yesButton").onclick = async function () {
//   document.getElementById("cameraPrompt").style.display = "none";

//   try {
//     const stream = await navigator.mediaDevices.getUserMedia({ video: true });

//     // Create video element BEFORE setting srcObject
//     video = document.createElement("video");
//     video.autoplay = true;
//     video.playsInline = true;
//     video.style.position = "fixed";
//     video.style.top = "0";
//     video.style.left = "0";
//     video.style.transform = "scaleX(-1)";
//     video.style.zIndex = "2";
//     video.style.opacity = "1";
//     video.width = 320;
//     video.height = 220;
//     document.body.appendChild(video);

//     video.srcObject = stream;

//     await initPoseDetection(); // Now safe to call
//   } catch (err) {
//     alert("Camera permission denied.");
//     console.error(err);
//   }
// };

//   document.getElementById("noButton").onclick = function () {
//     document.getElementById("cameraPrompt").style.display = "none";
//     alert("You choose not to enable camera tracking.");
//   };
// }

// async function initPoseDetection() {
//   const vision = await FilesetResolver.forVisionTasks(
//     "https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision/wasm"
//   );

//   poseLandmarker = await PoseLandmarker.createFromOptions(vision, {
//     baseOptions: {
//       modelAssetPath:
//         "https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task",
//     },
//     runningMode: "VIDEO",
//     numPoses: 1,
//   });


//   video.onloadedmetadata = () => {
//     video.play();
//     detectLoop();
//   };
// }

// const DETECTION_INTERVAL = globalThis.runtime?.globalVars?.DetectionSpeed || 85;

// async function detectLoop() {
//   const now = performance.now();
//   if (now - lastTimestamp < DETECTION_INTERVAL) {
//     requestAnimationFrame(detectLoop);
//     return;
//   }
//   lastTimestamp = now;

//   if (!poseLandmarker || video.readyState !== 4) {
//     requestAnimationFrame(detectLoop);
//     return;
//   }

//   const result = await poseLandmarker.detectForVideo(video, now);

//   if (result.landmarks && result.landmarks.length > 0) {
//     const landmarks = result.landmarks[0];

//     const leftHand = landmarks[19];
//     const rightHand = landmarks[20];

//     const canvas = document.querySelector("canvas");
//     const w = canvas.width;
//     const h = canvas.height;

//     globalThis.runtime.globalVars.LeftHandX = (1 - leftHand.x) * w;
//     globalThis.runtime.globalVars.LeftHandY = leftHand.y * h;
//     globalThis.runtime.globalVars.RightHandX = (1 - rightHand.x) * w;
//     globalThis.runtime.globalVars.RightHandY = rightHand.y * h;
//   }

//   requestAnimationFrame(detectLoop);
// }

// // Entry point
// runOnStartup(async runtime => {
//   globalThis.runtime = runtime;
//   createCameraPromptUI(); // Show Yes/No buttons first
// });







import { PoseLandmarker, FilesetResolver } from "https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision/vision_bundle.js";

let poseLandmarker, video, lastTimestamp = 0;

// ✅ Create a Yes/No camera permission UI
function createCameraPromptUI() {
  const prompt = document.createElement("div");
  prompt.id = "cameraPrompt";
  prompt.style.position = "fixed";
  prompt.style.zIndex = "1000";
  prompt.style.top = "0";
  prompt.style.left = "0";
  prompt.style.width = "100vw";
  prompt.style.height = "100vh";
  prompt.style.background = "rgba(0,0,0,0.8)";
  prompt.style.display = "flex";
  prompt.style.flexDirection = "column";
  prompt.style.justifyContent = "center";
  prompt.style.alignItems = "center";

  prompt.innerHTML = `
    <p style="color: white; font-size: 18px;">Do you want to enable the camera?</p>
    <button id="yesButton" style="margin: 10px; padding: 10px 20px;">Yes</button>
    <button id="noButton" style="margin: 10px; padding: 10px 20px;">No</button>
  `;

  document.body.appendChild(prompt);

  // ✅ If YES → request camera
  document.getElementById("yesButton").onclick = async function () {
    document.getElementById("cameraPrompt").remove();
    await initCamera();
  };

  // ❌ If NO → fallback message
  document.getElementById("noButton").onclick = function () {
    document.getElementById("cameraPrompt").remove();

    const msg = document.createElement("div");
    msg.innerText = "Camera disabled.\nGame will run without tracking.";
    msg.style.position = "fixed";
    msg.style.top = "50%";
    msg.style.left = "50%";
    msg.style.transform = "translate(-50%, -50%)";
    msg.style.color = "white";
    msg.style.background = "rgba(0,0,0,0.7)";
    msg.style.padding = "20px";
    msg.style.borderRadius = "12px";
    msg.style.zIndex = "999";
    document.body.appendChild(msg);
  };
}

// ✅ Camera setup + pose detection
async function initCamera() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ video: true });

    // Create video element
    video = document.createElement("video");
    video.autoplay = true;
    video.playsInline = true;
    video.style.position = "fixed";
    video.style.top = "0";
    video.style.left = "0";
    video.style.transform = "scaleX(-1)";
    video.style.zIndex = "2";
    video.style.opacity = "1";
    video.width = 320;
    video.height = 220;
    document.body.appendChild(video);

    video.srcObject = stream;

    // Init Mediapipe after video is ready
    video.onloadedmetadata = () => {
      video.play();
      initPoseDetection();
    };
  } catch (err) {
    console.warn("⚠️ Camera not available or permission denied:", err);
  }
}

async function initPoseDetection() {
  const vision = await FilesetResolver.forVisionTasks(
    "https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision/wasm"
  );

  poseLandmarker = await PoseLandmarker.createFromOptions(vision, {
    baseOptions: {
      modelAssetPath:
        "https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task",
    },
    runningMode: "VIDEO",
    numPoses: 1,
  });

  detectLoop();
}

const DETECTION_INTERVAL = globalThis.runtime?.globalVars?.DetectionSpeed || 85;

// ✅ Pose detection loop
async function detectLoop() {
  const now = performance.now();
  if (now - lastTimestamp < DETECTION_INTERVAL) {
    requestAnimationFrame(detectLoop);
    return;
  }
  lastTimestamp = now;

  if (!poseLandmarker || video.readyState !== 4) {
    requestAnimationFrame(detectLoop);
    return;
  }

  const result = await poseLandmarker.detectForVideo(video, now);

  if (result.landmarks && result.landmarks.length > 0) {
    const landmarks = result.landmarks[0];
    const leftHand = landmarks[19];
    const rightHand = landmarks[20];

    const canvas = document.querySelector("canvas");
    const w = canvas.width;
    const h = canvas.height;

    globalThis.runtime.globalVars.LeftHandX = (1 - leftHand.x) * w;
    globalThis.runtime.globalVars.LeftHandY = leftHand.y * h;
    globalThis.runtime.globalVars.RightHandX = (1 - rightHand.x) * w;
    globalThis.runtime.globalVars.RightHandY = rightHand.y * h;
  }

  requestAnimationFrame(detectLoop);
}

// ✅ Entry point
runOnStartup(async runtime => {
  globalThis.runtime = runtime;
  createCameraPromptUI(); // show Yes/No first
});
