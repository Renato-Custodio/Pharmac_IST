import { Module } from '@nestjs/common';
import { PharmacyService } from './pharmacy.service';
import { CoreModule } from 'src/core/core.module';
import { AuthModule } from 'src/auth/auth.module';
import { AccountService } from 'src/account/account.service';
import { PharmacyController } from './pharmacy.controller';

@Module({
  imports: [CoreModule, AuthModule],
  providers: [PharmacyService, AccountService],
  controllers: [PharmacyController],
})
export class PharmacyModule {}
