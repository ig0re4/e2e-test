function Tree() {
	this.socket = new Socket('localhost', 8080, this);
	this.panel = new Panel(this, this.socket);
	this.self = this;
	this.currentNode = null;
}

Tree.prototype.init = function() {
	var that = this;
	$("#tree").dynatree({
		title: "Scenario Manager",
		onActivate: function(node) {
			$("#echoActive").text(node.data.title);
			if( node.data.url )
				window.open(node.data.url, node.data.target);
		},
		onDeactivate: function(node) {
			$("#echoSelected").text("-");
		},
		onFocus: function(node) {
			that.panel.show(node);
			that.currentNode = node;
			$("#echoFocused").text(node.data.title);
		},
		onBlur: function(node) {
			$("#echoFocused").text("-");
		},
		onLazyRead: function(node){
			
		}
	});

	$("#btnSetTitle").click(function(){
		var node = $("#tree").dynatree("getActiveNode");
		if( !node ) return;
		node.setTitle(node.data.title + ", " + new Date());
		// this is a shortcut for
		// node.fromDict({title: node.data.title + new Date()});
	});
	$("#btnFromDict").click(function(){
		var node = $("#tree").dynatree("getActiveNode");
		if( !node ) return;
		// alert(JSON.stringify(node.toDict(true)));
		// Set node data and - optionally - replace children
		node.fromDict({
			title: node.data.title + new Date(),
			children: [{title: "t1"}, {title: "t2"}]
		});
	});
	$("#btnToggleExpand").click(function(){
		$("#tree").dynatree("getRoot").visit(function(node){
			node.toggleExpand();
		});
		return false;
	});
	$("#btnCollapseAll").click(function(){
		$("#tree").dynatree("getRoot").visit(function(node){
			node.expand(false);
		});
		return false;
	});
	$("#btnExpandAll").click(function(){
		$("#tree").dynatree("getRoot").visit(function(node){
			node.expand(true);
		});
		return false;
	});
	$("#btnRefresh").click(function(){
		$("#tree").dynatree("getRoot").removeChildren();
		that.socket.getAll();
		return false;
	});
};

Tree.prototype.start = function() {
	this.socket.connect();
	this.socket.getAll();
};

Tree.prototype.addAll = function(data) {
	$("#tree").dynatree("getRoot").addChild(data);
}

Tree.prototype.create = function(node) {
	if(this.currentNode !== null){
		this.currentNode.addChild(node);
		return true;
	}else{
		return false;
	}
};

Tree.prototype.update = function(node) {
	if(this.currentNode !== null){
		this.currentNode.data.title	= node.title;
		this.currentNode.data.value	= node.value;
		this.currentNode.data.type = node.type;
		return true;
	}else{
		return false;
	}
};

Tree.prototype.remove = function() {
	if(this.currentNode !== null){
		this.currentNode.remove();
		return true;
	}else{
		return false;
	}
};
