# Интеграционная платформа Annette-Sphera

This project is based on ideas and source code of Valery Lobachev (https://github.com/valerylobachev)

Нужно первоначально сгенерировать схему, для этого в настройках conf/application.conf прописываем
```core.initDb.createSchema = true```

## Собираем клиент:
```
cd annette-frontend-sphera/ng/
npm run build
```

## Запускаем sbt task:
```
sbt ~annette-sphera-server/reStart
```

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

## Users page:
![users page](https://raw.githubusercontent.com/duberg/annette-sphera/master/screenshot/s_listusers.png)

## Signin page:
![signin page](https://raw.githubusercontent.com/duberg/annette-sphera/master/screenshot/s_signin.png)

## Signup confirmation (example email template):
![signup page](https://raw.githubusercontent.com/duberg/annette-sphera/master/screenshot/s_emailconfirmation.png)