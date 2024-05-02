import { Module } from '@nestjs/common';
import { AccountModule } from './account/account.module';
import { CoreModule } from './core/core.module';
import { ConfigModule } from '@nestjs/config';

@Module({
  imports: [ConfigModule.forRoot(), AccountModule, CoreModule],
})
export class AppModule {}
