import { TenancyModule } from './tenancy.module';

describe('TenancyModule', () => {
  let tenancyModule: TenancyModule;

  beforeEach(() => {
    tenancyModule = new TenancyModule();
  });

  it('should create an instance', () => {
    expect(tenancyModule).toBeTruthy();
  });
});
