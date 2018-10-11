# Integration platform Annette-Sphera

This project is based on ideas and source code of Valery Lobachev (https://github.com/valerylobachev)

- Install sbt, postgresql, protoc-3.2.0, nodejs, @angular/cli: ```/scripts/install-project-tools.sh```

- Create database annette_sphera
```bash
sudo -u postgres psql postgres
create database annette_sphera;
\q
```

- Generate postgresql schema. In conf/application.conf set this:
```core.initDb.createSchema = true```

- Build frontend:
```bash
cd annette-frontend-sphera/ng/
npm install
npm run build
```

- Start sbt task:
```bash
cd ../../
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

## Tenants page with pagination:
![tenants page](https://raw.githubusercontent.com/duberg/annette-sphera/master/screenshot/s_tenants.png)


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

## Contributing

Contributions are *very* welcome!

If you see an issue that you'd like to see fixed, the best way to make it happen is to help out by submitting a pull request implementing it.

Refer to the [CONTRIBUTING.md](docs/CONTRIBUTING.md) and  [CODE_OF_CONDUCT.md](docs/CODE_OF_CONDUCT.md) file for more
 details about the workflow, and general hints on how to prepare your pull request. You can also ask for 
 clarifications or guidance in GitHub issues.


## License

Annette-Sphera is Open Source and available under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
