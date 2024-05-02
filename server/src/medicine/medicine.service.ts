import { Injectable } from '@nestjs/common';
import { Medicine } from '@prisma/client';
import { PrismaService } from 'src/core/prisma.service';

@Injectable()
export class MedicineService {
  constructor(private prismaService: PrismaService) {}

  async getMedicines(pageNumber: number): Promise<Medicine[]> {
    const skip = (pageNumber - 1) * 10;
    const medicines = await this.prismaService.medicine.findMany({
      skip: skip,
      take: 10,
    });
    return medicines;
  }

  async getQuery(
    query: string,
    pageNumber: number,
  ): Promise<Medicine[] | null> {
    const skip = (pageNumber - 1) * 10;
    const medicine = await this.prismaService.medicine.findMany({
      where: {
        AND: [{ name: { contains: query } }, { purpose: { contains: query } }],
      },
      skip: skip,
      take: 10,
    });

    return medicine ?? null;
  }

  async createMedicine(data: {
    name: string;
    purpose: string;
    picture: string;
    quantity: number;
  }): Promise<Medicine> {
    return await this.prismaService.medicine.create({
      data: {
        name: data.name,
        purpose: data.purpose,
        picture: data.picture,
        quantity: data.quantity,
      },
    });
  }
}
