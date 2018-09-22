obj = {
    name: 'lastSession', // наименование актора
    // package: 'annette.core.services', // корневой пакет, опционально
    modelPackage: 'annette.core.domain.tenancy.model',  // пакет с моделью данных
    actorPackage: 'annette.core.domain.tenancy.model',  // пакет с моделью данных
    actorName: 'LastSessionActor',
    stateName: 'LastSessionState',
    objectName: 'LastSessionService',

    // imports: [ 'java.util.UUID' ],

    entities: [
        {
            name: 'lastSession',
            pluralName: 'lastSessions',
            commands: [
                {create: true},
                {update: true},
                {del: true}
            ],
            queries: [
                {byId: true},
                {all: true}
            ]
        }
    ]
}