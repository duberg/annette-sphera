import { MasterdataModule } from './masterdata.module';

describe('MasterdataModule', () => {
  let masterdataModule: MasterdataModule;

  beforeEach(() => {
    masterdataModule = new MasterdataModule();
  });

  it('should create an instance', () => {
    expect(masterdataModule).toBeTruthy();
  });
});
