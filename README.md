# Интеграционная платформа Annette-Sphera

This project is based on ideas and source code of Valery Lobachev (https://github.com/valerylobachev)

Установить sbt, nodejs, postgresql (или cassandra), protoc-3.2.0 
```/scripts/install-project-tools.sh```

Нужно первоначально сгенерировать схему, для этого в настройках conf/application.conf прописываем
```core.initDb.createSchema = true```

## Собираем клиент:
```bash
cd annette-frontend-sphera/ng/
npm install
npm run build
```

## Запускаем sbt task:
```bash
sbt ~annette-sphera-server/reStart
```

## Users page with pagination:
![users page](https://raw.githubusercontent.com/duberg/annette-sphera/master/screenshot/s_listusers.png)

```scala
  val userRoutes: Route = (pathPrefix("users") & authorized) { implicit session =>
    createUser ~ getUser ~ updateUser ~ deleteUser ~ listUsers
  }
  
  def listUsers(implicit session: Session): Route = (get & pagination) { page =>
       ...
  }
```

## Signin page:
![signin page](https://raw.githubusercontent.com/duberg/annette-sphera/master/screenshot/s_signin.png)

## Директива авторизации akka-http:
```scala
 /**
   * = Authorization directive =
   *
   * Для проверки прав доступа нужно извлечь uri ресурса, http метод и сессию пользователя.
   */
  val authorized: Directive1[Session] = {
    (extractUri & extractMethod & authenticated) tflatMap {
      case (uri, httpMethod, session) =>
        authorizeAsync(authorizationCheck(uri, httpMethod, session)) & provide(session)
    }
  }
```

## Signup confirmation (example email template):
![signup page](https://raw.githubusercontent.com/duberg/annette-sphera/master/screenshot/s_emailconfirmation.png)