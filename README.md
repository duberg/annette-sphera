# Integration platform Annette-Sphera

This project is based on ideas and source code of Valery Lobachev (https://github.com/valerylobachev)

- Install sbt, postgresql, protoc-3.2.0, nodejs, @angular/cli: ```/scripts/install-project-tools.sh```

- Create database annette_sphera
```bash
sudo -u postgres psql postgres
create database annette_sphera;
\q
```

- Generate postgresql schema:
```bash
sbt mg init
sbt ~mg migrate
```

- Generate default application data. In conf/application.conf set this:
```core.initDb.createSchema = true```

- Build frontend:
```bash
cd annette-frontend-sphera/ng/
npm install
npm run build
```

- Start sbt task:
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

## Authorization directive akka-http:
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