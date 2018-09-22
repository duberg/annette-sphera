obj = {
    name: 'openSession', // наименование актора
    // package: 'annette.core.services', // корневой пакет, опционально
    modelPackage: 'annette.core.domain.tenancy.model',  // пакет с моделью данных
    actorPackage: 'annette.core.domain.tenancy.model',  // пакет с моделью данных
    actorName: 'OpenSessionActor',
    stateName: 'OpenSessionState',
    objectName: 'OpenSessionService',

    // imports: [ 'java.util.UUID' ],

    entities: [
        {
            name: 'openSession',
            pluralName: 'openSessions',
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