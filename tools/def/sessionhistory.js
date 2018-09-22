obj = {
    name: 'sessionHistory', // наименование актора
    // package: 'annette.core.services', // корневой пакет, опционально
    modelPackage: 'annette.core.domain.tenancy.model',  // пакет с моделью данных
    actorPackage: 'annette.core.domain.tenancy.model',  // пакет с моделью данных
    actorName: 'SessionHistoryActor',
    stateName: 'SessionHistoryState',
    objectName: 'SessionHistoryService',

    // imports: [ 'java.util.UUID' ],

    entities: [
        {
            name: 'sessionHistory',
            pluralName: 'sessionHistory',
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