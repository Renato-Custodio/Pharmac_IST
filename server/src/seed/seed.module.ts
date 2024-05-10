import { Module } from '@nestjs/common';
import { CoreModule } from 'src/core/core.module';
import { SeedService } from './seed.service';
import { PharmacyModule } from 'src/pharmacy/pharmacy.module';
import { AccountModule } from 'src/account/account.module';
import { MedicineModule } from 'src/medicine/medicine.module';

@Module({
  imports: [CoreModule, PharmacyModule, AccountModule, MedicineModule],
  providers: [SeedService],
})
export class SeedModule {}
