import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { PrismaService } from '../core/prisma.service';
import { hash } from 'bcrypt';
import { PharmacyService } from 'src/pharmacy/pharmacy.service';

const LAT_MAX = 38.81;
const LAT_MIN = 38.69;
const LNG_MAX = -9.05;
const LNG_MIN = -9.3;

@Injectable()
export class SeedService implements OnModuleInit {
  private readonly logger = new Logger(SeedService.name);

  constructor(
    private prismaService: PrismaService,
    private pharmacyService: PharmacyService,
  ) {}

  private async cleanUp() {
    await this.prismaService.stock.deleteMany();
    await this.prismaService.mapChunk.deleteMany();
    await this.prismaService.pharmacy.deleteMany();
    await this.prismaService.medicine.deleteMany();
    await this.prismaService.account.deleteMany();
  }

  private async createAccounts() {
    const AccountData = [];
    for (let i = 1; i <= 10; i++) {
      const account = {
        username: `account${i}`,
        password: (await hash(`account${i}`, 10)).toString(),
      };
      AccountData.push(account);
    }
    await this.prismaService.account.createMany({
      data: AccountData,
    });
  }

  private async createMedicines() {
    const medicineData = [];
    for (let i = 1; i <= 50; i++) {
      const medicine = {
        name: `Medicine${i}`,
        purpose: `Purpose${i}`,
        picture: '',
      };
      medicineData.push(medicine);
    }
    await this.prismaService.medicine.createMany({
      data: medicineData,
    });
  }

  private async createPharmacies() {
    for (let i = 1; i <= 261; i++) {
      const pharmacy = {
        name: `Pharmacy${i}`,
        location: {
          lat: parseFloat(
            (Math.random() * (LAT_MAX - LAT_MIN) + LAT_MIN).toFixed(6),
          ), // Random latitude between 100 and -200
          lng: parseFloat(
            (Math.random() * (LNG_MAX - LNG_MIN) + LNG_MIN).toFixed(6),
          ), // Random longitude between 100 and -200
        },
      };

      await this.pharmacyService.createPharmacy(pharmacy);
    }
  }

  private async randomlyAssignMedicinesToPharmacies(): Promise<void> {
    const pharmacies = await this.pharmacyService.getPharmacies();
    const medicines = await this.prismaService.medicine.findMany();

    for (const medicine of medicines) {
      const randomPharmacy =
        pharmacies[Math.floor(Math.random() * pharmacies.length)];
      const quantity = Math.floor(Math.random() * 100); // You can adjust the quantity as needed
      await this.pharmacyService.addMedicine({
        medicineID: medicine.id,
        quantity: quantity,
        pharmacyId: randomPharmacy.id,
      });
    }
  }

  async onModuleInit() {
    this.logger.warn('Cleaning up old data...');
    await this.cleanUp();

    this.logger.log('Creating accounts...');
    await this.createAccounts();

    this.logger.log('Creating medicines...');
    await this.createMedicines();

    this.logger.log('Creating pharmacies...');
    await this.createPharmacies();

    this.logger.log('Randomly Assigning Medicines to Pharmacies...');
    await this.randomlyAssignMedicinesToPharmacies();
  }
}
