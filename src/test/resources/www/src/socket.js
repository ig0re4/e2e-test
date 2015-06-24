function Socket(url, port, tree) {
    this.url = url;
    this.port = port;
	this.tree = tree;
}

Socket.prototype.connect = function() {
    this.ws = new WebSocket("ws://" + this.url + ":" + this.port + "/websocket");
};

Socket.prototype.getAll = function () {
	var that = this;
    this.send('{"command": "get_all"}', function () {
		that.ws.onmessage = function (event) {
			var json = JSON.parse(event.data);
			that.tree.addAll(json);
		}
    });
};

Socket.prototype.create = function (message, node) {
	var that = this;
	this.send(message, function () {
		that.ws.onmessage = function (event) {
			that.tree.create(node);
		}
    });
};

Socket.prototype.update = function (message, node) {
    var that = this;
	this.send(message, function () {
		that.ws.onmessage = function (event) {
			that.tree.update(node);
		}
    });
};

Socket.prototype.remove = function (message) {
    var that = this;
	this.send(message, function () {
		that.ws.onmessage = function (event) {
			that.tree.remove();
		}
    });
};

Socket.prototype.send = function (message, callback) {
	var that = this;
    this.waitForConnection(function () {
        that.ws.send(message);
        if (typeof callback !== 'undefined') {
          callback();
        }
    }, 1000);
};

Socket.prototype.waitForConnection = function (callback, interval) {
    if (this.ws.readyState === 1) {
        callback();
    } else {
        var that = this;
        setTimeout(function () {
            that.waitForConnection(callback);
        }, interval);
    }
};

