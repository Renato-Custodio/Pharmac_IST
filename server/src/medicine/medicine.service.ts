import { Injectable, OnModuleInit } from '@nestjs/common';
import { Medicine } from '@prisma/client';
import { PrismaService } from 'src/core/prisma.service';
import { CreateMedicineDto } from './dtos/create-Medicine.dto';

@Injectable()
export class MedicineService implements OnModuleInit {
  constructor(private prismaService: PrismaService) {}
  async onModuleInit() {
    await this.prismaService.medicine.deleteMany();
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

  async getMedicines(
    query: string = '',
    pageNumber: number = 1,
  ): Promise<Partial<Medicine>[] | null> {
    const skip = (pageNumber - 1) * 10;
    const medicine = await this.prismaService.medicine.findMany({
      where: {
        OR: [{ name: { contains: query } }, { purpose: { contains: query } }],
      },
      select: {
        id: true,
        name: true,
        purpose: true,
        picture: true,
      },
      skip: skip,
      take: 10,
    });
    return medicine ?? null;
  }

  async createMedicine(dto: CreateMedicineDto): Promise<Medicine> {
    return await this.prismaService.medicine.create({
      data: {
        name: dto.name,
        purpose: dto.purpose,
        picture: '',
      },
    });
  }
}
