import { Module } from '@nestjs/common';
import { AccountModule } from './account/account.module';
import { CoreModule } from './core/core.module';
import { ConfigModule } from '@nestjs/config';
import { MedicineModule } from './medicine/medicine.module';
import { PharmacyModule } from './pharmacy/pharmacy.module';
import { ServeStaticModule } from '@nestjs/serve-static';
import { join } from 'path';
import { SeedModule } from './seed/seed.module';

@Module({
  imports: [
    ServeStaticModule.forRoot({
      rootPath: join(__dirname, '..', 'images', 'pharmacies'),
      serveRoot: '/images/pharmacies',
    }),
    ServeStaticModule.forRoot({
      rootPath: join(__dirname, '..', 'images', 'medicines'),
      serveRoot: '/images/medicines',
    }),
    ConfigModule.forRoot(),
    AccountModule,
    CoreModule,
    MedicineModule,
    PharmacyModule,
    SeedModule,
  ],
})
export class AppModule {}
