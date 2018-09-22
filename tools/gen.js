#!/usr/bin/env node
var Def = /** @class */ (function () {
    function Def() {
    }
    Def.prototype.build = function (o) {
        var _this = this;
        this.name = o.name;
        this.package = o.package;
        this.modelPackage = o.modelPackage || o.package + '.model';
        this.actorPackage = o.actorPackage || o.package + '.' + o.name;
        this.actorName = o.actorName || capFirst(o.name) + 'Actor';
        this.stateName = o.stateName || capFirst(o.name) + 'State';
        this.objectName = o.objectName || capFirst(o.name);
        this.imports = o.imports || [];
        this.entities = o.entities.map(function (e) { return _this.buildEntity(e); });
    };
    Def.prototype.buildEntity = function (e) {
        var entity = new Entity();
        entity.build(this, e);
        return entity;
    };
    Def.prototype.generateObject = function () {
        var o = this;
        var imports = o.imports.map(function (s) { return "import " + s; }).join('\n');
        var entities = o.entities.map(function (e) {
            var code = '';
            // команды
            if (e.commands)
                code = code + e.commands.map(function (c) { return c.commandClassDeclaration(); }).join('\n') + '\n';
            //  запросы
            if (e.queries)
                code = code + e.queries.map(function (q) { return q.commandClassDeclaration(); }).join('\n') + '\n';
            code = code + '\n';
            // события команд
            if (e.commands)
                code = code + e.commands.map(function (c) { return c.eventClassDeclaration(); }).join('\n') + '\n';
            // ответы на запросы
            code = code + '\n' + e.responsesDeclaration;
            // нетиповые ответы на запросы
            if (e.queries) {
                code = code + e.queries
                    .filter(function (q) { return q.response; })
                    .map(function (q) { return q.responseClassDeclaration(); }).join('\n') + '\n';
            }
            return code;
        }).join('\n');
        var code = "package " + o.actorPackage + "\n" + imports + "\n\nimport akka.actor.Props\nimport annette.core.persistence.Persistence.{ PersistentCommand, PersistentEvent, PersistentQuery }\nimport " + o.modelPackage + "._\n\nobject " + o.objectName + " {\n\n  sealed trait Command extends PersistentCommand\n  sealed trait Query extends PersistentQuery\n  sealed trait Event extends PersistentEvent\n  sealed trait Response\n\n  object EntryAlreadyExists extends Response\n  object EntryNotFound extends Response\n  \n" + entities + "\n\n  def props(id: String, state: " + o.stateName + " = " + o.stateName + "()) = Props(classOf[" + o.actorName + "], id, state)\n}\n    ";
        return code;
    };
    Def.prototype.stateUpdatedMethod = function () {
        var o = this;
        var updatedCases = o.entities.map(function (e) {
            var caseSt = e.commands.map(function (c) {
                var params = extractParams(c.params);
                return "      case " + o.objectName + "." + c.eventClass() + "(" + params + ") => " + c.methodName() + "(" + params + ")";
            }).join('\n');
            return caseSt;
        }).join('\n');
        return "  override def updated(event: Persistence.PersistentEvent) = {\n    event match {\n" + updatedCases + "    \n    }\n  }";
    };
    Def.prototype.generateState = function () {
        var o = this;
        var imports = o.imports.map(function (s) { return "import " + s; }).join('\n');
        var stateDecl = o.entities.map(function (e) {
            return "    " + e.pluralName + ": " + e.entityMap + " = Map.empty";
        }).join(',\n');
        var methods = o.entities.map(function (e) {
            var commandMethodsCode = e.commands.map(function (c) { return c.stateMethod(); }).join('\n') + '\n';
            var queryMethodsCode = e.queries.map(function (q) { return q.stateMethod(); }).join('\n') + '\n';
            var existsMethod = e.existMethod();
            return commandMethodsCode + queryMethodsCode + existsMethod;
        }).join('\n');
        var code = "package " + o.actorPackage + "\n" + imports + "\n\nimport annette.core.persistence.Persistence\nimport annette.core.persistence.Persistence.PersistentState\nimport " + o.modelPackage + "._\n\ncase class " + o.stateName + "(\n" + stateDecl + "\n  ) extends PersistentState[" + o.stateName + "] {\n  \n" + methods + "\n\n" + o.stateUpdatedMethod() + "\n}";
        return code;
    };
    Def.prototype.actorBehaviorMethod = function () {
        var o = this;
        var commandCases = o.entities.map(function (e) {
            var commandSt = e.commands.map(function (c) {
                var params = extractParams(c.params);
                return "    case " + o.objectName + "." + c.commandClass() + "(" + params + ") => " + c.methodName() + "(state, " + params + ")";
            }).join('\n');
            var querySt = e.queries.map(function (q) {
                var params = extractParams(q.params);
                if (q.all) {
                    return "    case " + o.objectName + "." + q.queryClass() + " => " + q.methodName() + "(state)";
                }
                else if (q.type == 'object') {
                    return "    case " + o.objectName + "." + q.queryClass() + " => " + q.methodName() + "(state)";
                }
                else {
                    return "    case " + o.objectName + "." + q.queryClass() + "(" + params + ") => " + q.methodName() + "(state, " + params + ")";
                }
            }).join('\n');
            return commandSt + '\n' + querySt + '\n';
        }).join('\n');
        return "  def behavior(state: " + o.stateName + "): Receive = {\n" + commandCases + "    \n  }";
    };
    Def.prototype.generateActor = function () {
        var o = this;
        var imports = o.imports.map(function (s) { return "import " + s; }).join('\n');
        var methods = o.entities.map(function (e) {
            var commandMethodsCode = e.commands.map(function (c) { return c.actorMethod(); }).join('\n') + '\n';
            var queryMethodsCode = e.queries.map(function (q) { return q.actorMethod(); }).join('\n');
            return commandMethodsCode + queryMethodsCode;
        }).join('\n');
        var code = "package " + o.actorPackage + "\n" + imports + "\n\nimport akka.Done\nimport annette.core.persistence.Persistence._\nimport " + o.modelPackage + "._\n\nclass " + o.actorName + "(val id: String, val initState: " + o.stateName + ") extends PersistentStateActor[" + o.stateName + "] {\n  \n" + methods + "\n\n" + o.actorBehaviorMethod() + "\n  \n}\n  ";
        return code;
    };
    return Def;
}());
var Entity = /** @class */ (function () {
    function Entity() {
    }
    Entity.prototype.build = function (o, e) {
        var _this = this;
        this.o = o;
        this.name = e.name;
        this.capName = capFirst(e.name);
        this.pluralName = e.pluralName || e.name + 's';
        this.commands = e.commands.map(function (c) { return new Command(_this, c); });
        this.queries = e.queries.map(function (q) { return new Query(_this, q); });
        this.entityMap = "Map[" + this.capName + ".Id, " + this.capName + "]";
        this.responsesDeclaration = "  case class Single" + this.capName + "(maybeEntry: Option[" + this.capName + "]) extends Response\n" +
            ("  case class Multiple" + capFirst(this.pluralName) + "(entries: " + this.entityMap + ") extends Response\n");
    };
    Entity.prototype.existMethod = function () {
        return "  def " + this.name + "Exists(id: " + this.capName + ".Id): Boolean = " + this.pluralName + ".get(id).isDefined\n";
    };
    return Entity;
}());
var Command = /** @class */ (function () {
    function Command(e, c) {
        this.e = e;
        this.create = c.create || false;
        this.update = c.update || false;
        this.del = c.del || false;
        this.verb = c.verb || '';
        this.pstVerb = c.pstVerb || '';
        this.params = c.params || '';
        this.response = c.response || '';
        if (c.create) {
            this.verb = c.verb || 'create';
            this.pstVerb = c.pstVerb || 'created';
            this.params = c.params || "entry: " + e.capName;
        }
        else if (c.update) {
            this.verb = c.verb || 'update';
            this.pstVerb = c.pstVerb || 'updated';
            this.params = c.params || "entry: " + e.capName + "Update";
        }
        else if (c.del) {
            this.verb = c.verb || 'delete';
            this.pstVerb = c.pstVerb || 'deleted';
            this.params = c.params || "id: " + e.capName + ".Id";
        }
    }
    Command.prototype.commandClass = function () {
        var e = this.e;
        return "" + capFirst(this.verb) + e.capName + "Cmd";
    };
    Command.prototype.eventClass = function () {
        var e = this.e;
        return "" + e.capName + capFirst(this.pstVerb) + "Evt";
    };
    Command.prototype.methodName = function () {
        var e = this.e;
        return "" + this.verb + e.capName;
    };
    Command.prototype.commandClassDeclaration = function () {
        var e = this.e;
        return "  case class " + this.commandClass() + "(" + this.params + ") extends Command";
    };
    Command.prototype.eventClassDeclaration = function () {
        var e = this.e;
        return "  case class " + this.eventClass() + "(" + this.params + ") extends Event";
    };
    Command.prototype.stateMethod = function () {
        var body;
        if (this.create) {
            body = "    if (" + this.e.pluralName + ".get(entry.id).isDefined) throw new IllegalArgumentException\n    else copy(" + this.e.pluralName + " = " + this.e.pluralName + " + (entry.id -> entry))";
        }
        else if (this.update) {
            body = "    " + this.e.pluralName + "\n      .get(entry.id)\n      .map{\n        e => \n          val updatedEntry = e.copy(\n            // TODO: name = entry.name.getOrElse(e.name)\n          )\n          copy(" + this.e.pluralName + " = " + this.e.pluralName + " + (entry.id -> updatedEntry))\n      }\n      .getOrElse(throw new IllegalArgumentException)";
        }
        else if (this.del) {
            body = "    if (" + this.e.pluralName + ".get(id).isEmpty) throw new IllegalArgumentException\n    else copy(" + this.e.pluralName + " = " + this.e.pluralName + " - id)";
        }
        else {
            body = "    // TODO: \u0412\u0441\u0442\u0430\u0432\u044C\u0442\u0435 \u043A\u043E\u0434 \u043C\u0435\u0442\u043E\u0434\u0430";
        }
        var methodCode = "  def " + this.methodName() + "(" + this.params + "): " + this.e.o.stateName + " = {\n" + body + "\n  }\n";
        return methodCode;
    };
    Command.prototype.actorMethod = function () {
        {
            var c = this;
            var e = this.e;
            var o = this.e.o;
            var body;
            if (c.create) {
                body = "    if (state." + e.name + "Exists(entry.id)) sender ! " + o.objectName + ".EntryAlreadyExists\n    else {\n      persist(" + o.objectName + "." + c.eventClass() + "(entry)) { event =>\n        changeState(state.updated(event))\n        sender ! Done\n      }\n    }";
            }
            else if (c.update) {
                body = "    if (state." + e.name + "Exists(entry.id)) {\n      persist(" + o.objectName + "." + c.eventClass() + "(entry)) { event =>\n        changeState(state.updated(event))\n        sender ! Done\n      } \n    } else {\n            sender ! " + o.objectName + ".EntryNotFound\n      }";
            }
            else if (c.del) {
                body = "    if (state." + e.name + "Exists(id)) {\n      persist(" + o.objectName + "." + c.eventClass() + "(id)) { event =>\n        changeState(state.updated(event))\n        sender ! Done\n      } \n    } else {\n            sender ! " + o.objectName + ".EntryNotFound\n      }        ";
            }
            else {
                body = "    // TODO: \u0412\u0441\u0442\u0430\u0432\u044C\u0442\u0435 \u043A\u043E\u0434 \u043C\u0435\u0442\u043E\u0434\u0430";
            }
            var methodCode = "  def " + c.methodName() + "(state: " + o.stateName + ", " + c.params + "): Unit = {\n" + body + "\n  }\n";
            return methodCode;
        }
    };
    return Command;
}());
var Query = /** @class */ (function () {
    function Query(e, q) {
        this.e = e;
        this.byId = q.byId || false;
        this.all = q.all || false;
        this.prefix = q.prefix || '';
        this.suffix = q.suffix || '';
        this.response = q.response || '';
        if (q.byId) {
            this.type = q.type || 'case class';
            this.suffix = q.suffix || 'ById';
            this.params = q.params || "id: " + e.capName + ".Id";
        }
        else if (q.all) {
            this.type = q.type || 'object';
            this.prefix = q.prefix || 'All';
            this.params = q.params || "";
        }
        else {
            this.type = q.type || 'case class';
            this.params = q.params || "id: " + e.capName + ".Id";
        }
    }
    Query.prototype.queryClass = function () {
        var e = this.e;
        if (this.all)
            return "Find" + capFirst(this.prefix) + capFirst(e.pluralName) + capFirst(this.suffix);
        else
            return "Find" + capFirst(this.prefix) + e.capName + capFirst(this.suffix);
    };
    Query.prototype.methodName = function () {
        var e = this.e;
        if (this.all)
            return "find" + capFirst(this.prefix) + capFirst(e.pluralName);
        else
            return "find" + capFirst(this.prefix) + e.capName + capFirst(this.suffix);
    };
    Query.prototype.commandClassDeclaration = function () {
        if (this.all)
            return "  " + this.type + " " + this.queryClass() + " extends Query";
        else if (this.type == 'object')
            return "  " + this.type + " " + this.queryClass() + " extends Query";
        else
            return "  " + this.type + " " + this.queryClass() + "(" + this.params + ") extends Query";
    };
    Query.prototype.responseClassDeclaration = function () {
        return "  case class " + this.response + "  extends Response";
    };
    Query.prototype.stateMethod = function () {
        var q = this;
        var e = this.e;
        var method;
        if (q.byId) {
            method = "  def " + q.methodName() + "(" + q.params + "): Option[" + e.capName + "] = " + e.pluralName + ".get(id)\n";
        }
        else if (q.all) {
            method = "  def " + q.methodName() + ": " + e.entityMap + " = " + e.pluralName + "\n";
        }
        else {
            method = "  def " + q.methodName() + "(" + q.params + "): /* TODO: \u0432\u043E\u0437\u0432\u0440\u0430\u0449\u0430\u0435\u043C\u044B\u0439 \u0442\u0438\u043F */  = {\n     // TODO: \u0412\u0441\u0442\u0430\u0432\u044C\u0442\u0435 \u043A\u043E\u0434 \u043C\u0435\u0442\u043E\u0434\u0430\n   }\n";
        }
        return method;
    };
    Query.prototype.actorMethod = function () {
        var q = this;
        var e = this.e;
        var o = this.e.o;
        var method;
        var params = extractParams(q.params);
        if (q.byId) {
            method = "  def " + q.methodName() + "(state: " + o.stateName + ", " + q.params + "): Unit = \n    sender ! " + o.objectName + ".Single" + e.capName + "(state." + q.methodName() + "(" + params + "))\n";
        }
        else if (q.all) {
            method = "  def " + q.methodName() + "(state: " + o.stateName + "): Unit =\n    sender ! " + o.objectName + ".Multiple" + capFirst(e.pluralName) + "(state." + q.methodName() + ")\n";
        }
        else {
            method = "  def " + q.methodName() + "(state: " + o.stateName + ", " + q.params + "): Unit  = {\n     // TODO: \u0412\u0441\u0442\u0430\u0432\u044C\u0442\u0435 \u043A\u043E\u0434 \u043C\u0435\u0442\u043E\u0434\u0430\n   }\n";
        }
        return method;
    };
    return Query;
}());
function capFirst(s) {
    return s.charAt(0).toUpperCase() + s.substr(1, s.length - 1);
}
function extractParams(params) {
    var res = params
        .split(',')
        .map(function (p) {
        var param = p.split(':');
        return param[0];
    }).join(', ');
    return res;
}
// ******************************* CODE *******************************
var fs = require('fs');
var argv = process.argv;
if (argv.length < 3) {
    console.log("Use node script <json file>");
    process.exit(1);
}
var file = argv[2];
var obj = eval(fs.readFileSync(file, 'utf8'));
var def = new Def();
def.build(obj);
var objectCode = def.generateObject();
var stateCode = def.generateState();
var actorCode = def.generateActor();
var genDir = 'gen';
if (!fs.existsSync(genDir)) {
    fs.mkdirSync(genDir);
}
fs.writeFile(genDir + "/" + def.objectName + ".scala", objectCode, function (err) {
    if (err)
        console.log(err);
    else
        console.log("Object file " + def.objectName + ".scala was saved!");
});
fs.writeFile(genDir + "/" + def.stateName + ".scala", stateCode, function (err) {
    if (err)
        console.log(err);
    else
        console.log("State file " + def.stateName + ".scala was saved!");
});
fs.writeFile(genDir + "/" + def.actorName + ".scala", actorCode, function (err) {
    if (err)
        console.log(err);
    else
        console.log("State file " + def.actorName + ".scala was saved!");
});
