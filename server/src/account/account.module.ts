import { Module } from '@nestjs/common';
import { AccountService } from './account.service';
import { AccountController } from './account.controller';
import { CoreModule } from 'src/core/core.module';
import { AuthModule } from 'src/auth/auth.module';

@Module({
  imports: [CoreModule, AuthModule],
  providers: [AccountService],
  controllers: [AccountController],
})
export class AccountModule {}
