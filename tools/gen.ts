#!/usr/bin/env node

//import {require} from "../annette-frontend/src/test";

interface IDef {
    name: string
    package?: string
    modelPackage?: string
    actorPackage?: string
    actorName?: string
    stateName?: string
    objectName?: string
    imports?: string[]

    entities: IEntity[]
}

class Def {
  name: string
  package?: string
  modelPackage?: string
  actorPackage?: string
  actorName?: string
  stateName?: string
  objectName?: string
  imports?: string[]

  entities: Entity[]

  build(o: IDef) {
    this.name = o.name
    this.package = o.package
    this.modelPackage = o.modelPackage || o.package + '.model'
    this.actorPackage = o.actorPackage || o.package + '.' + o.name
    this.actorName = o.actorName || capFirst(o.name) + 'Actor'
    this.stateName = o.stateName || capFirst(o.name) + 'State'
    this.objectName = o.objectName || capFirst(o.name)
    this.imports = o.imports || []
    this.entities = o.entities.map( e => this.buildEntity(e))

  }

   buildEntity(e: IEntity): Entity {
     let entity = new Entity()
     entity.build(this, e)
     return entity
  }

  generateObject(): string {
    let o = this
    let imports = o.imports.map(s => `import ${s}`).join('\n')

    let entities = o.entities.map(e => {
      var code = ''
      // команды
      if (e.commands) code = code + e.commands.map(c => c.commandClassDeclaration()).join('\n') + '\n'
      //  запросы
      if (e.queries) code = code + e.queries.map(q => q.commandClassDeclaration()).join('\n') + '\n'
      code = code + '\n'
      // события команд
      if (e.commands) code = code + e.commands.map(c => c.eventClassDeclaration()).join('\n') + '\n'
      // ответы на запросы
      code = code + '\n' + e.responsesDeclaration
      // нетиповые ответы на запросы
      if (e.queries) {
        code = code + e.queries
            .filter(q => q.response)
            .map(q => q.responseClassDeclaration()).join('\n') + '\n'
      }

      return code
    }).join('\n')


    var code = `package ${o.actorPackage}
${imports}

import akka.actor.Props
import annette.core.persistence.Persistence.{ PersistentCommand, PersistentEvent, PersistentQuery }
import ${o.modelPackage}._

object ${o.objectName} {

  sealed trait Command extends PersistentCommand
  sealed trait Query extends PersistentQuery
  sealed trait Event extends PersistentEvent
  sealed trait Response

  object EntryAlreadyExists extends Response
  object EntryNotFound extends Response
  
${entities}

  def props(id: String, state: ${o.stateName} = ${o.stateName}()) = Props(classOf[${o.actorName}], id, state)
}
    `

    return code

  }

  stateUpdatedMethod() {
    let o = this
    let updatedCases = o.entities.map(e => {
      let caseSt = e.commands.map(c => {
        let params =  extractParams(c.params)
        return `      case ${o.objectName}.${c.eventClass()}(${params}) => ${c.methodName()}(${params})`
      }).join('\n')
      return caseSt
    }).join('\n')
    return `  override def updated(event: Persistence.PersistentEvent) = {
    event match {
${updatedCases}    
    }
  }`
  }

  generateState(): string {
    let o = this
    let imports = o.imports.map(s => `import ${s}`).join('\n')
    let stateDecl = o.entities.map( e =>
        `    ${e.pluralName}: ${e.entityMap} = Map.empty`
    ).join(',\n')

    let methods = o.entities.map(e => {
          let commandMethodsCode = e.commands.map(c => c.stateMethod()).join('\n') + '\n'
          let queryMethodsCode = e.queries.map(q => q.stateMethod()).join('\n') + '\n'
          let existsMethod = e.existMethod()
          return commandMethodsCode + queryMethodsCode + existsMethod
        }
    ).join('\n')



    var code = `package ${o.actorPackage}
${imports}

import annette.core.persistence.Persistence
import annette.core.persistence.Persistence.PersistentState
import ${o.modelPackage}._

case class ${o.stateName}(
${stateDecl}
  ) extends PersistentState[${o.stateName}] {
  
${methods}

${o.stateUpdatedMethod()}
}`
    return code

  }

  actorBehaviorMethod() {
    let o = this
    let commandCases = o.entities.map(e => {
      let commandSt = e.commands.map(c => {
        let params = extractParams(c.params)
        return `    case ${o.objectName}.${c.commandClass()}(${params}) => ${c.methodName()}(state, ${params})`
      }).join('\n')
      let querySt = e.queries.map(q => {
        let params = extractParams(q.params)
        if (q.all) {
          return `    case ${o.objectName}.${q.queryClass()} => ${q.methodName()}(state)`
        } else if (q.type == 'object') {
          return `    case ${o.objectName}.${q.queryClass()} => ${q.methodName()}(state)`
        } else {
          return `    case ${o.objectName}.${q.queryClass()}(${params}) => ${q.methodName()}(state, ${params})`
        }
      }).join('\n')


      return commandSt + '\n' + querySt + '\n'
    }).join('\n')

    return `  def behavior(state: ${o.stateName}): Receive = {
${commandCases}    
  }`
  }

  generateActor(): string {
    let o = this
    let imports = o.imports.map(s => `import ${s}`).join('\n')


    let methods = o.entities.map(e => {
          let commandMethodsCode = e.commands.map(c => c.actorMethod()).join('\n') + '\n'

          let queryMethodsCode = e.queries.map( q => q.actorMethod()).join('\n')
          return commandMethodsCode + queryMethodsCode
        }
    ).join('\n')



    var code = `package ${o.actorPackage}
${imports}

import akka.Done
import annette.core.persistence.Persistence._
import ${o.modelPackage}._

class ${o.actorName}(val id: String, val initState: ${o.stateName}) extends PersistentStateActor[${o.stateName}] {
  
${methods}

${o.actorBehaviorMethod()}
  
}
  `
    return code
  }

}

interface IEntity {
    name: string
    capName: string
    pluralName?: string
    commands: ICommand[]
    queries: IQuery[]
}



class Entity{
  name: string
  capName?: string
  pluralName?: string
  commands: Command[]
  queries: Query[]
  o: Def

  responsesDeclaration: string
  entityMap: string

  build(o: Def, e: IEntity) {
    this.o = o

    this.name = e.name
    this.capName = capFirst(e.name)
    this.pluralName = e.pluralName || e.name + 's'

    this.commands = e.commands.map( c => new Command(this, c))
    this.queries = e.queries.map( q => new Query(this, q))

    this.entityMap = `Map[${this.capName}.Id, ${this.capName}]`

    this.responsesDeclaration = `  case class Single${this.capName}(maybeEntry: Option[${this.capName}]) extends Response\n` +
        `  case class Multiple${capFirst(this.pluralName)}(entries: ${this.entityMap}) extends Response\n`

  }

  existMethod() {
    return `  def ${this.name}Exists(id: ${this.capName}.Id): Boolean = ${this.pluralName}.get(id).isDefined\n`
  }

}

interface ICommand {
    create?: boolean
    update?: boolean
    del?: boolean
    verb?: string
    pstVerb?: string
    params?: string
    response?: string
}

class Command {
  create?: boolean
  update?: boolean
  del?: boolean
  verb?: string
  pstVerb?: string
  params?: string
  response?: string
  e: Entity

  constructor(e: Entity, c: ICommand) {
    this.e = e
    this.create = c.create || false
    this.update = c.update || false
    this.del = c.del || false
    this.verb = c.verb || ''
    this.pstVerb = c.pstVerb || ''
    this.params = c.params || ''
    this.response = c.response || ''
    if (c.create) {
      this.verb = c.verb || 'create'
      this.pstVerb = c.pstVerb || 'created'
      this.params = c.params || `entry: ${e.capName}`
    } else if (c.update) {
      this.verb = c.verb || 'update'
      this.pstVerb = c.pstVerb || 'updated'
      this.params = c.params || `entry: ${e.capName}Update`
    } else if (c.del) {
      this.verb = c.verb || 'delete'
      this.pstVerb = c.pstVerb || 'deleted'
      this.params = c.params || `id: ${e.capName}.Id`
    }


  }

  commandClass() {
    let e = this.e
    return `${capFirst(this.verb)}${e.capName}Cmd`
  }
  eventClass() {
    let e = this.e
    return `${e.capName}${capFirst(this.pstVerb)}Evt`
  }
  methodName() {
    let e = this.e
    return `${this.verb}${e.capName}`
  }
  commandClassDeclaration() {
    let e = this.e
    return `  case class ${this.commandClass()}(${this.params}) extends Command`
  }
  eventClassDeclaration() {
    let e = this.e
    return `  case class ${this.eventClass()}(${this.params}) extends Event`
  }
  
  stateMethod() {
    var body
    if (this.create) {
      body = `    if (${this.e.pluralName}.get(entry.id).isDefined) throw new IllegalArgumentException
    else copy(${this.e.pluralName} = ${this.e.pluralName} + (entry.id -> entry))`
    } else if (this.update){
      body = `    ${this.e.pluralName}
      .get(entry.id)
      .map{
        e => 
          val updatedEntry = e.copy(
            // TODO: name = entry.name.getOrElse(e.name)
          )
          copy(${this.e.pluralName} = ${this.e.pluralName} + (entry.id -> updatedEntry))
      }
      .getOrElse(throw new IllegalArgumentException)`
    } else if (this.del) {
      body = `    if (${this.e.pluralName}.get(id).isEmpty) throw new IllegalArgumentException
    else copy(${this.e.pluralName} = ${this.e.pluralName} - id)`
    } else {
      body = `    // TODO: Вставьте код метода`
    }
    let methodCode = `  def ${this.methodName()}(${this.params}): ${this.e.o.stateName} = {
${body}
  }\n`
    return methodCode
  }

  actorMethod() {
    {
      let c = this
      let e = this.e
      let o = this.e.o
      var body
      if (c.create) {
        body = `    if (state.${e.name}Exists(entry.id)) sender ! ${o.objectName}.EntryAlreadyExists
    else {
      persist(${o.objectName}.${c.eventClass()}(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    }`
      } else if (c.update){
        body = `    if (state.${e.name}Exists(entry.id)) {
      persist(${o.objectName}.${c.eventClass()}(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      } 
    } else {
            sender ! ${o.objectName}.EntryNotFound
      }`
      } else if (c.del) {
        body = `    if (state.${e.name}Exists(id)) {
      persist(${o.objectName}.${c.eventClass()}(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      } 
    } else {
            sender ! ${o.objectName}.EntryNotFound
      }        `
      } else {
        body = `    // TODO: Вставьте код метода`
      }
      let methodCode = `  def ${c.methodName()}(state: ${o.stateName}, ${c.params}): Unit = {
${body}
  }\n`
      return methodCode
    }
  }
}


interface IQuery {
    byId?: boolean
    all?: boolean
    type: string
    prefix?: string
    suffix?: string
    params?: string
    response?: string
}


class Query {
  byId?: boolean
  all?: boolean
  type: string
  prefix?: string
  suffix?: string
  params?: string
  response?: string

  e: Entity

  constructor(e: Entity, q: IQuery) {
    this.e = e
    this.byId = q.byId || false
    this.all = q.all || false
    this.prefix = q.prefix || ''
    this.suffix = q.suffix || ''
    this.response = q.response || ''
    if (q.byId) {
      this.type = q.type || 'case class'
      this.suffix = q.suffix || 'ById'
      this.params = q.params || `id: ${e.capName}.Id`
    } else if (q.all) {
      this.type = q.type || 'object'
      this.prefix = q.prefix || 'All'
      this.params = q.params || ``
    } else {
      this.type = q.type || 'case class'
      this.params = q.params || `id: ${e.capName}.Id`
    }
  }

  queryClass() {
    let e = this.e
    if (this.all) return `Find${capFirst(this.prefix)}${capFirst(e.pluralName)}${capFirst(this.suffix)}`
    else return `Find${capFirst(this.prefix)}${e.capName}${capFirst(this.suffix)}`
  }

  methodName() {
    let e = this.e
    if (this.all) return `find${capFirst(this.prefix)}${capFirst(e.pluralName)}`
    else return `find${capFirst(this.prefix)}${e.capName}${capFirst(this.suffix)}`
  }

  commandClassDeclaration() {
    if (this.all)
      return`  ${this.type} ${this.queryClass()} extends Query`
    else if (this.type == 'object')
      return `  ${this.type} ${this.queryClass()} extends Query`
    else
      return `  ${this.type} ${this.queryClass()}(${this.params}) extends Query`
  }

  responseClassDeclaration() {
    return `  case class ${this.response}  extends Response`
  }

  stateMethod() {
    let q = this
    let e = this.e
    var method
    if (q.byId) {
      method = `  def ${q.methodName()}(${q.params}): Option[${e.capName}] = ${e.pluralName}.get(id)\n`
    } else if (q.all) {
      method = `  def ${q.methodName()}: ${e.entityMap} = ${e.pluralName}\n`
    } else {
      method = `  def ${q.methodName()}(${q.params}): /* TODO: возвращаемый тип */  = {
     // TODO: Вставьте код метода
   }\n`
    }
    return method
  }

  actorMethod() {
    let q = this
    let e = this.e
    let o = this.e.o
    var method
    let params =  extractParams(q.params)
    if (q.byId) {
      method = `  def ${q.methodName()}(state: ${o.stateName}, ${q.params}): Unit = 
    sender ! ${o.objectName}.Single${e.capName}(state.${q.methodName()}(${params}))\n`
    } else if (q.all) {
      method = `  def ${q.methodName()}(state: ${o.stateName}): Unit =
    sender ! ${o.objectName}.Multiple${capFirst(e.pluralName)}(state.${q.methodName()})\n`
    } else {
      method = `  def ${q.methodName()}(state: ${o.stateName}, ${q.params}): Unit  = {
     // TODO: Вставьте код метода
   }\n`
    }
    return method
  }


}

function capFirst(s: string): string {
    return s.charAt(0).toUpperCase() + s.substr(1, s.length-1)
}

function extractParams(params: string): string {
    let res = params
        .split(',')
        .map(p => {
            let param = p.split(':')
          return param[0]
    }).join(', ')
  return res

}

// ******************************* CODE *******************************

let fs = require('fs');

let argv: any[] = process.argv
if (argv.length <3 ) {
    console.log("Use node script <json file>")
    process.exit(1);
}
let file = argv[2]

var obj = eval(fs.readFileSync(file, 'utf8')) as IDef;

let def = new Def()
def.build(obj)

let objectCode = def.generateObject()
let stateCode = def.generateState()
let actorCode = def.generateActor()

let genDir = 'gen'

if (!fs.existsSync(genDir)){
  fs.mkdirSync(genDir);
}

fs.writeFile(`${genDir}/${def.objectName}.scala`, objectCode, function(err) {
  if(err) console.log(err);
  else console.log(`Object file ${def.objectName}.scala was saved!`);
});

fs.writeFile(`${genDir}/${def.stateName}.scala`, stateCode, function(err) {
  if(err) console.log(err);
  else console.log(`State file ${def.stateName}.scala was saved!`);
});

fs.writeFile(`${genDir}/${def.actorName}.scala`, actorCode, function(err) {
  if(err) console.log(err);
  else console.log(`State file ${def.actorName}.scala was saved!`);
});




