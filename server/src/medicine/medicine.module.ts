import { Module } from '@nestjs/common';
import { MedicineService } from './medicine.service';
import { CoreModule } from 'src/core/core.module';
import { AuthModule } from 'src/auth/auth.module';
import { MedicineController } from './medicine.controller';

@Module({
  imports: [CoreModule, AuthModule],
  providers: [MedicineService],
  controllers: [MedicineController],
})
export class MedicineModule {}
