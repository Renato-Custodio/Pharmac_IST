import { Module } from '@nestjs/common';
import { AccountModule } from './account/account.module';
import { CoreModule } from './core/core.module';
import { ConfigModule } from '@nestjs/config';
import { MedicineModule } from './medicine/medicine.module';
import { PharmacyModule } from './pharmacy/pharmacy.module';

@Module({
  imports: [
    ConfigModule.forRoot(),
    AccountModule,
    CoreModule,
    MedicineModule,
    PharmacyModule,
  ],
})
export class AppModule {}
