/*
 * Cliente SSE robusto con reconexion automatica y backoff exponencial con jitter.
 *
 * Uso rapido:
 * const client = createTickerSseClient({
 *   url: "/api/v1/empleado/panel/ticker",
 *   withCredentials: true,
 *   onEstadoPanel: (data) => console.log(data)
 * });
 * client.start();
 */
(function (global) {
  "use strict";

  function defaultBackoff(attempt, baseDelayMs, maxDelayMs) {
    var exp = Math.min(maxDelayMs, baseDelayMs * Math.pow(2, attempt));
    var jitter = Math.floor(Math.random() * Math.max(250, Math.floor(exp * 0.25)));
    return exp + jitter;
  }

  function createTickerSseClient(options) {
    if (!options || !options.url) {
      throw new Error("createTickerSseClient requiere la propiedad url");
    }

    var cfg = {
      url: options.url,
      withCredentials: options.withCredentials === true,
      eventName: options.eventName || "estado_panel",
      baseDelayMs: Number(options.baseDelayMs || 1000),
      maxDelayMs: Number(options.maxDelayMs || 30000),
      staleTimeoutMs: Number(options.staleTimeoutMs || 45000),
      autoStart: options.autoStart === true,
      onEstadoPanel: typeof options.onEstadoPanel === "function" ? options.onEstadoPanel : function () {},
      onOpen: typeof options.onOpen === "function" ? options.onOpen : function () {},
      onClose: typeof options.onClose === "function" ? options.onClose : function () {},
      onReconnectScheduled: typeof options.onReconnectScheduled === "function" ? options.onReconnectScheduled : function () {},
      onError: typeof options.onError === "function" ? options.onError : function () {}
    };

    var es = null;
    var reconnectTimer = null;
    var staleTimer = null;
    var closedByUser = false;
    var attempt = 0;
    var connected = false;

    function clearTimers() {
      if (reconnectTimer) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
      }
      if (staleTimer) {
        clearTimeout(staleTimer);
        staleTimer = null;
      }
    }

    function resetStaleWatchdog() {
      if (staleTimer) {
        clearTimeout(staleTimer);
      }
      staleTimer = setTimeout(function () {
        if (!closedByUser) {
          forceReconnect("stale_timeout");
        }
      }, cfg.staleTimeoutMs);
    }

    function forceReconnect(reason) {
      cleanupConnection();
      scheduleReconnect(reason || "forced");
    }

    function cleanupConnection() {
      connected = false;
      if (es) {
        try {
          es.close();
        } catch (e) {
          // noop
        }
        es = null;
      }
    }

    function scheduleReconnect(reason) {
      if (closedByUser) {
        return;
      }
      var wait = defaultBackoff(attempt, cfg.baseDelayMs, cfg.maxDelayMs);
      cfg.onReconnectScheduled({ attempt: attempt + 1, waitMs: wait, reason: reason });
      attempt += 1;
      reconnectTimer = setTimeout(function () {
        open();
      }, wait);
    }

    function open() {
      if (closedByUser) {
        return;
      }
      clearTimers();
      cleanupConnection();

      es = new EventSource(cfg.url, { withCredentials: cfg.withCredentials });

      es.onopen = function () {
        connected = true;
        attempt = 0;
        cfg.onOpen();
        resetStaleWatchdog();
      };

      es.addEventListener(cfg.eventName, function (event) {
        resetStaleWatchdog();
        try {
          var payload = JSON.parse(event.data);
          cfg.onEstadoPanel(payload);
        } catch (err) {
          cfg.onError({ type: "parse_error", error: err, raw: event.data });
        }
      });

      es.onerror = function (err) {
        cfg.onError({ type: "sse_error", error: err });
        if (!closedByUser) {
          cleanupConnection();
          scheduleReconnect("sse_error");
        }
      };
    }

    function start() {
      closedByUser = false;
      open();
    }

    function stop() {
      closedByUser = true;
      clearTimers();
      cleanupConnection();
      cfg.onClose();
    }

    function isConnected() {
      return connected;
    }

    var api = {
      start: start,
      stop: stop,
      isConnected: isConnected,
      forceReconnect: forceReconnect
    };

    if (cfg.autoStart) {
      api.start();
    }

    return api;
  }

  global.createTickerSseClient = createTickerSseClient;
})(window);

