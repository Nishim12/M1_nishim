import type { Server as HttpServer } from 'node:http';
import type { Server as HttpsServer } from 'node:https';

import { Server as SocketIOServer, type Socket } from 'socket.io';

const COURSE_PIXEL_STREAM_URL = 'wss://8.229.22.124';
const RECONNECT_DELAY_MS = 1000;

// Opens a dedicated course-provided pixel stream for one Socket.io client and
// relays it verbatim, with no batching, delay, or reformatting, per Button 2's
// spec. A new stream starts a fresh image, so every visit begins from scratch.
function relayPixelsToClient(socket: Socket): void {
  let upstream: WebSocket | undefined;
  let reconnectTimer: NodeJS.Timeout | undefined;
  let clientGone = false;

  const connect = (): void => {
    const stream = new WebSocket(COURSE_PIXEL_STREAM_URL);
    upstream = stream;

    stream.addEventListener('message', (event: MessageEvent) => {
      const data = typeof event.data === 'string' ? event.data : '';
      try {
        const pixel = JSON.parse(data) as { x: number; y: number; color: string };
        socket.emit('pixel', pixel);
      } catch {
        // Ignore any non-JSON payload rather than crashing the relay.
      }
    });

    stream.addEventListener('close', () => {
      if (!clientGone) {
        reconnectTimer = setTimeout(connect, RECONNECT_DELAY_MS);
      }
    });

    stream.addEventListener('error', () => {
      stream.close();
    });
  };

  connect();

  socket.on('disconnect', () => {
    clientGone = true;
    clearTimeout(reconnectTimer);
    upstream?.close();
  });
}

export function attachLiveUpdates(server: HttpServer | HttpsServer): void {
  const io = new SocketIOServer(server);
  io.on('connection', relayPixelsToClient);
}
