# Интеграционная платформа Annette-Sphera

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

This project is based on ideas and source code of dmitry.rogachev (https://github.com/valerylobachev)