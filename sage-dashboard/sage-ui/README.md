## SAGE UI

This app is the React dashboard for live SAGE telemetry.

- Start the bridge from [`../bridge.js`](../bridge.js) (`npm install && npm start` inside `sage-dashboard/`).
- Run the UI with `npm run dev` inside `sage-dashboard/sage-ui`.
- The frontend listens for `telemetry_update` events from the Socket.IO bridge on port `6006`.
