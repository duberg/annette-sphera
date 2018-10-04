# Интеграционная платформа Annette-Sphera

This project is based on ideas and source code of dmitry.rogachev (https://github.com/valerylobachev)

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

Users page:
![users page](https://raw.githubusercontent.com/duberg/annette-sphera/master/screenshot/Screenshot%20from%202018-10-04%2014-00-08.png)

Signin page:
![login page](https://raw.githubusercontent.com/duberg/annette-sphera/master/screenshot/Screenshot%20from%202018-10-04%2014-00-49.png)