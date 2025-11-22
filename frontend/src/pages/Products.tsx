import React, {useState, useEffect} from 'react';
import {Table, Typography, Button, Modal, Form, Input, message, Row, Col, Checkbox} from 'antd';
import axios from 'axios';
import {useKeycloak} from "@react-keycloak/web";
import {ColumnsType} from "antd/es/table";

const {Title} = Typography;

interface Product {
    id: number;
    name: string;
    description: string;
    pricePerDay: number;
    available: boolean;
    stock: number;
}

const Products = () => {
    const [data, setData] = useState<Product[]>([]);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingProduct, setEditingProduct] = useState(null);
    const [loading, setLoading] = useState(false);
    const [filters, setFilters] = useState({name: '', minPrice: '', maxPrice: ''});
    const [form] = Form.useForm();
    const {keycloak} = useKeycloak();

    const token = keycloak.token;
    const headers = {Authorization: `Bearer ${token}`};

    const fetchProducts = async () => {
        setLoading(true);
        try {
            const token = keycloak?.token;
            if (!token) {
                message.error("Token not found!");
                return;
            }

            const response = await axios.get<Product[]>(
                "http://localhost:8080/items/my",
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            console.log(response.data);

            const sortedData = (response.data ?? []).sort((a, b) => a.id - b.id);

            console.log(sortedData);

            setData(sortedData);
        } catch (error) {
            console.error(error);
            message.error("Failed to fetch products");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchProducts();
    }, [keycloak?.token]);

    const showModal = async () => {
    };

    const handleSearch = () => {
    };

    const handleReset = () => {
    };

    const handleSubmit = async () => {
    };

    const handleCancel = () => {
    };

    const columns: ColumnsType<Product> = [
        {title: 'ID', dataIndex: 'id', key: 'id', sorter: (a, b) => a.id - b.id, defaultSortOrder: 'ascend'},
        {title: 'Name', dataIndex: 'name', key: 'name'},
        {title: 'Description', dataIndex: 'description', key: 'description'},
        {title: 'Price Per Day', dataIndex: 'pricePerDay', key: 'pricePerDay'},
        {
            title: "Available",
            dataIndex: "available",
            key: "available",
            render: (available: boolean) => <Checkbox checked={available} />,
        },
        {title: 'Stock', dataIndex: 'stock', key: 'stock'},
        {
            title: 'Actions',
            key: 'actions',
            render: (_, record) => (
                <>
                    <Button /*onClick={() => showModal(record)}*/ style={{marginRight: 8}}>Edit</Button>
                    <Button /*onClick={() => handleDelete(record.id)}*/ danger>Delete</Button>
                </>
            ),
        },
    ];

    return (
        <div>
            {/* Header with Title and Add Product Button */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                <Title level={3}>Products</Title>
                <Button type="primary" onClick={() => showModal()}>Add Product</Button>
            </div>

            {/* Search Filters */}
            <Row gutter={16} style={{ marginBottom: 16, display: 'flex', alignItems: 'center' }}>
                <Col span={6}>
                    <Input
                        placeholder="Search by name"
                        value={filters.name}
                        onChange={(e) => setFilters({ ...filters, name: e.target.value })}
                    />
                </Col>
                <Col span={6}>
                    <Input
                        placeholder="Min Price"
                        type="number"
                        value={filters.minPrice}
                        onChange={(e) => setFilters({ ...filters, minPrice: e.target.value })}
                    />
                </Col>
                <Col span={6}>
                    <Input
                        placeholder="Max Price"
                        type="number"
                        value={filters.maxPrice}
                        onChange={(e) => setFilters({ ...filters, maxPrice: e.target.value })}
                    />
                </Col>
                <Col span={6} style={{ display: 'flex', gap: '8px' }}>
                    <Button type="primary" onClick={handleSearch}>Search</Button>
                    <Button onClick={handleReset}>Reset</Button>
                </Col>
            </Row>

            {/* Products Table */}
            <Table
                columns={columns}
                dataSource={data}
                rowKey="id"
                loading={loading}
                pagination={false}
            />

            {/* Modal for Add/Edit Product */}
            <Modal
                title={editingProduct ? 'Edit Product' : 'Add Product'}
                visible={isModalVisible}
                onOk={handleSubmit}
                onCancel={handleCancel}
            >
                <Form form={form} layout="vertical">
                    <Form.Item
                        name="name"
                        label="Product Name"
                        rules={[{ required: true, message: 'Please enter product name' }]}
                    >
                        <Input />
                    </Form.Item>
                    <Form.Item
                        name="price"
                        label="Price"
                        rules={[{ required: true, message: 'Please enter product price' }]}
                    >
                        <Input type="number" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default Products;