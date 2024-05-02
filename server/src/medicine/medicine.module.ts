import { Module } from '@nestjs/common';
import { MedicineService } from './medicine.service';
import { AccountController } from './medicine.controller';
import { CoreModule } from 'src/core/core.module';
import { AuthModule } from 'src/auth/auth.module';

@Module({
  imports: [CoreModule, AuthModule],
  providers: [MedicineService],
  controllers: [AccountController],
})
export class AccountModule {}
